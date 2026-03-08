package com.claymodeler.ui

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import com.claymodeler.R
import com.claymodeler.examples.ExampleInfo

class ExampleBrowserDialog(
    context: Context,
    private val examples: List<ExampleInfo>,
    private val onExampleSelected: (String) -> Unit
) : Dialog(context) {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_examples)
        setTitle("Example Models")
        
        val listView = findViewById<ListView>(R.id.list_examples)
        
        val items = examples.map { "${it.name} - ${it.difficulty}" }
        val adapter = ArrayAdapter(context, android.R.layout.simple_list_item_1, items)
        listView.adapter = adapter
        
        listView.setOnItemClickListener { _, _, position, _ ->
            onExampleSelected(examples[position].filename)
            dismiss()
        }
    }
}
