package ru.vladimirov.rockettest.rockettest

import android.content.res.Configuration
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import kotlinx.android.synthetic.main.activity_main.*
import android.widget.ArrayAdapter
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_image.*
import kotlinx.android.synthetic.main.part_items_container.*
import android.support.v7.app.AlertDialog
import android.widget.SeekBar
import kotlinx.android.synthetic.main.alert_size.*


class MainActivity : AppCompatActivity() {

    private var width:Int = 40
    private var height:Int = 30
    private var speed:Int = 30

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        generate.setOnClickListener {
            container.removeAllViews()

            for (i in 0..1) {
                val holder = AlgViewHolder(layoutInflater.inflate(R.layout.item_image, container, false))

                holder.setup()

                container.addView(holder.containerView)
            }
        }

        size.setOnClickListener {

            val holder = SizeViewHolder(layoutInflater.inflate(R.layout.alert_size, null, false))

            holder.setup()

            val ad = AlertDialog.Builder(this).create()
            ad.setCancelable(true)
            ad.setTitle(getString(R.string.alert_title))
            ad.setView(holder.containerView)
            ad.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.alert_button_save)) { _, _ -> holder.save() }
            ad.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.alert_button_cancel)) { _, _ -> Unit }
            ad.show()

        }

        seek_speed.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {

            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                speed = progress
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}

            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
    }

    inner class SizeViewHolder(override val containerView:View) : LayoutContainer {

        fun setup() {
            input_width.setText(width.toString())
            input_height.setText(height.toString())
        }

        fun save() {
            width = input_width.text.toString().toInt()
            height = input_height.text.toString().toInt()
        }

    }

    inner class AlgViewHolder(override val containerView:View) : LayoutContainer {

        fun setup() {

            val verticalMode = resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT

            algView.initialize(width, height, verticalMode, speed, 1)

            val adapter = ArrayAdapter<String>(containerView.context, android.R.layout.simple_spinner_item, listOf("Alg 1", "Alg 2", "Alg 3"))
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter

            spinner.onItemSelectedListener = object : OnItemSelectedListener {

                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    algView.alg = position
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }
        }
    }

}
