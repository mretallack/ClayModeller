#!/usr/bin/env kotlin

// Script to generate example .clay models
// Run with: kotlinc -script generate_examples.kts

import java.io.File
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.zip.CRC32
import kotlin.math.*

data class Vector3(val x: Float, val y: Float, val z: Float) {
    operator fun plus(other: Vector3) = Vector3(x + other.x, y + other.y, z + other.z)
    operator fun minus(other: Vector3) = Vector3(x - other.x, y - other.y, z - other.z)
    operator fun times(scalar: Float) = Vector3(x * scalar, y * scalar, z * scalar)
    fun length() = sqrt(x * x + y * y + z * z)
    fun normalize(): Vector3 {
        val len = length()
        return if (len > 0f) Vector3(x / len, y / len, z / len) else this
    }
}

data class Face(val v1: Int, val v2: Int, val v3: Int)

class ClayModel {
    val vertices = mutableListOf<Vector3>()
    val faces = mutableListOf<Face>()
    val normals = mutableListOf<Vector3>()
    var lightPosition = Vector3(2f, 3f, 2f)
    var lightIntensity = 1f
    
    fun createIcosphere(subdivisions: Int) {
        val t = (1f + sqrt(5f)) / 2f
        
        vertices.addAll(listOf(
            Vector3(-1f, t, 0f), Vector3(1f, t, 0f), Vector3(-1f, -t, 0f), Vector3(1f, -t, 0f),
            Vector3(0f, -1f, t), Vector3(0f, 1f, t), Vector3(0f, -1f, -t), Vector3(0f, 1f, -t),
            Vector3(t, 0f, -1f), Vector3(t, 0f, 1f), Vector3(-t, 0f, -1f), Vector3(-t, 0f, 1f)
        ).map { it.normalize() })
        
        faces.addAll(listOf(
            Face(0, 11, 5), Face(0, 5, 1), Face(0, 1, 7), Face(0, 7, 10), Face(0, 10, 11),
            Face(1, 5, 9), Face(5, 11, 4), Face(11, 10, 2), Face(10, 7, 6), Face(7, 1, 8),
            Face(3, 9, 4), Face(3, 4, 2), Face(3, 2, 6), Face(3, 6, 8), Face(3, 8, 9),
            Face(4, 9, 5), Face(2, 4, 11), Face(6, 2, 10), Face(8, 6, 7), Face(9, 8, 1)
        ))
        
        repeat(subdivisions) { subdivide() }
        vertices.replaceAll { it.normalize() }
        calculateNormals()
    }
    
    private fun subdivide() {
        val newFaces = mutableListOf<Face>()
        val midpointCache = mutableMapOf<Pair<Int, Int>, Int>()
        
        fun getMidpoint(v1: Int, v2: Int): Int {
            val key = if (v1 < v2) Pair(v1, v2) else Pair(v2, v1)
            return midpointCache.getOrPut(key) {
                val mid = (vertices[v1] + vertices[v2]) * 0.5f
                vertices.add(mid)
                vertices.size - 1
            }
        }
        
        for (face in faces) {
            val a = getMidpoint(face.v1, face.v2)
            val b = getMidpoint(face.v2, face.v3)
            val c = getMidpoint(face.v3, face.v1)
            
            newFaces.add(Face(face.v1, a, c))
            newFaces.add(Face(face.v2, b, a))
            newFaces.add(Face(face.v3, c, b))
            newFaces.add(Face(a, b, c))
        }
        
        faces.clear()
        faces.addAll(newFaces)
    }
    
    private fun calculateNormals() {
        normals.clear()
        repeat(vertices.size) { normals.add(Vector3(0f, 0f, 0f)) }
        
        for (face in faces) {
            val v1 = vertices[face.v1]
            val v2 = vertices[face.v2]
            val v3 = vertices[face.v3]
            
            val edge1 = v2 - v1
            val edge2 = v3 - v1
            val normal = Vector3(
                edge1.y * edge2.z - edge1.z * edge2.y,
                edge1.z * edge2.x - edge1.x * edge2.z,
                edge1.x * edge2.y - edge1.y * edge2.x
            )
            
            normals[face.v1] = normals[face.v1] + normal
            normals[face.v2] = normals[face.v2] + normal
            normals[face.v3] = normals[face.v3] + normal
        }
        
        normals.replaceAll { it.normalize() }
    }
    
    fun save(filename: String) {
        val metadata = buildString {
            appendLine("name=$filename")
            appendLine("created=${System.currentTimeMillis()}")
            appendLine("vertexCount=${vertices.size}")
            appendLine("faceCount=${faces.size}")
            appendLine("light_x=${lightPosition.x}")
            appendLine("light_y=${lightPosition.y}")
            appendLine("light_z=${lightPosition.z}")
            appendLine("light_intensity=$lightIntensity")
        }
        val metadataBytes = metadata.toByteArray(Charsets.UTF_8)
        
        val vertexDataSize = vertices.size * 12
        val faceDataSize = faces.size * 12
        val normalDataSize = normals.size * 12
        
        RandomAccessFile(File(filename), "rw").use { raf ->
            raf.writeInt(0x434C4159) // CLAY
            raf.writeInt(1) // version
            raf.writeInt(metadataBytes.size)
            raf.writeInt(0) // checksum placeholder
            
            raf.write(metadataBytes)
            
            val vertexBuffer = ByteBuffer.allocate(vertexDataSize).order(ByteOrder.LITTLE_ENDIAN)
            vertices.forEach { vertexBuffer.putFloat(it.x).putFloat(it.y).putFloat(it.z) }
            raf.write(vertexBuffer.array())
            
            val faceBuffer = ByteBuffer.allocate(faceDataSize).order(ByteOrder.LITTLE_ENDIAN)
            faces.forEach { faceBuffer.putInt(it.v1).putInt(it.v2).putInt(it.v3) }
            raf.write(faceBuffer.array())
            
            val normalBuffer = ByteBuffer.allocate(normalDataSize).order(ByteOrder.LITTLE_ENDIAN)
            normals.forEach { normalBuffer.putFloat(it.x).putFloat(it.y).putFloat(it.z) }
            raf.write(normalBuffer.array())
            
            // Calculate checksum
            raf.seek(16)
            val dataBytes = ByteArray((raf.length() - 16).toInt())
            raf.readFully(dataBytes)
            val crc = CRC32()
            crc.update(dataBytes)
            raf.seek(12)
            raf.writeInt(crc.value.toInt())
        }
        
        println("Created: $filename (${vertices.size} vertices, ${faces.size} faces)")
    }
}

// Generate examples
File("examples").mkdirs()

// 1. Sphere - basic starting model
ClayModel().apply {
    createIcosphere(3)
    save("examples/sphere.clay")
}

// 2. Cube - lower subdivision for flatter look
ClayModel().apply {
    createIcosphere(2)
    save("examples/cube.clay")
}

// 3. Vase - elongated sphere
ClayModel().apply {
    createIcosphere(3)
    vertices.replaceAll { Vector3(it.x * 0.7f, it.y * 1.5f, it.z * 0.7f) }
    calculateNormals()
    save("examples/vase.clay")
}

// 4. Character head - slightly squashed sphere
ClayModel().apply {
    createIcosphere(3)
    vertices.replaceAll { Vector3(it.x * 1.1f, it.y * 1.2f, it.z * 0.9f) }
    calculateNormals()
    save("examples/character.clay")
}

// 5. Abstract - irregular shape
ClayModel().apply {
    createIcosphere(3)
    vertices.replaceAll { 
        val noise = sin(it.x * 3f) * cos(it.y * 3f) * 0.2f
        it * (1f + noise)
    }
    calculateNormals()
    save("examples/abstract.clay")
}

println("\nAll examples generated successfully!")
