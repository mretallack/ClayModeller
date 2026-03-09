package com.claymodeler.examples

import com.claymodeler.model.ClayModel
import com.claymodeler.model.Vector3
import com.claymodeler.file.FileManager
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import java.io.File

@RunWith(RobolectricTestRunner::class)
class GenerateExamples {
    
    @Test
    fun generateAllExamples() {
        val context = RuntimeEnvironment.getApplication()
        val fileManager = FileManager(context)
        val outputDir = File("app/src/main/assets/examples")
        outputDir.mkdirs()
        
        // 1. Sphere
        ClayModel().apply {
            initialize(3)
            val file = fileManager.save(this, "sphere")
            file.copyTo(File(outputDir, "sphere.clay"), overwrite = true)
            println("Created sphere.clay")
        }
        
        // 2. Cube - lower subdivision
        ClayModel().apply {
            initialize(2)
            val file = fileManager.save(this, "cube")
            file.copyTo(File(outputDir, "cube.clay"), overwrite = true)
            println("Created cube.clay")
        }
        
        // 3. Vase - elongated
        ClayModel().apply {
            initialize(3)
            vertices.replaceAll { Vector3(it.x * 0.7f, it.y * 1.5f, it.z * 0.7f) }
            recalculateNormals()
            val file = fileManager.save(this, "vase")
            file.copyTo(File(outputDir, "vase.clay"), overwrite = true)
            println("Created vase.clay")
        }
        
        // 4. Character - squashed
        ClayModel().apply {
            initialize(3)
            vertices.replaceAll { Vector3(it.x * 1.1f, it.y * 1.2f, it.z * 0.9f) }
            recalculateNormals()
            val file = fileManager.save(this, "character")
            file.copyTo(File(outputDir, "character.clay"), overwrite = true)
            println("Created character.clay")
        }
        
        // 5. Abstract - wavy
        ClayModel().apply {
            initialize(3)
            vertices.replaceAll { 
                val noise = kotlin.math.sin(it.x * 3f) * kotlin.math.cos(it.y * 3f) * 0.2f
                Vector3(it.x * (1f + noise), it.y * (1f + noise), it.z * (1f + noise))
            }
            recalculateNormals()
            val file = fileManager.save(this, "abstract")
            file.copyTo(File(outputDir, "abstract.clay"), overwrite = true)
            println("Created abstract.clay")
        }
        
        println("All examples generated in ${outputDir.absolutePath}")
    }
    
    private fun ClayModel.recalculateNormals() {
        normals.clear()
        repeat(vertices.size) { normals.add(Vector3(0f, 0f, 0f)) }
        
        for (face in faces) {
            val v1 = vertices[face.v1]
            val v2 = vertices[face.v2]
            val v3 = vertices[face.v3]
            
            val edge1 = v2 - v1
            val edge2 = v3 - v1
            val normal = edge1.cross(edge2)
            
            normals[face.v1] = normals[face.v1] + normal
            normals[face.v2] = normals[face.v2] + normal
            normals[face.v3] = normals[face.v3] + normal
        }
        
        normals.replaceAll { it.normalize() }
    }
}
