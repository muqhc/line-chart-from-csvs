package io.github.muqhc

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import io.kvision.*
import io.kvision.chart.*
import io.kvision.core.*
import io.kvision.form.upload.upload
import io.kvision.form.upload.uploadInput
import io.kvision.html.*
import io.kvision.panel.root
import io.kvision.types.contentType
import io.kvision.utils.getContent
import org.w3c.files.Blob
import org.w3c.files.FileReader

class App : Application() {
    companion object {
        val rootId = "root"
        val chartId = "myChart"

        val chartColorList = listOf(
            Color.name(Col.GRAY),
            Color.name(Col.RED),
            Color.name(Col.PINK),
            Color.name(Col.BLUE),
            Color.name(Col.AQUA),
        )
    }

    var csvText: String? = null
    var chart: Component? = null

    override fun start() {
        root(rootId) {
            p {
                input(InputType.FILE) {
                    onChange {
                        TODO("error")
                        val file = getElementD().files[0]
                        val fr = FileReader()
                        fr.readAsText(file as Blob)
                        csvText = fr.result as String


                        chart?.let { it1 -> this@root.remove(it1) }
                        chart = null

                        if (csvText != null) {
                            chart = this@root.myChart()
                        }
                    }
                }
            }
        }
//        chart?.let { it1 -> this@root.remove(it1) }
//        chart = null
//
//        if (csvText != null) {
//            chart = this@root.myChart()
//        }
    }

    fun Container.myChart(): Component {
        val read = csvReader().readAll(csvText!!)
        val transformed = read[0].mapIndexed { i,_ ->
            read.map { it[i] }
        }
        val labels = transformed[0].drop(1)
        val dataBagList = transformed.drop(1).map {
            it.drop(1).map { it.toDoubleOrNull() ?: Double.NaN }
        }

        val myDataSets =
            DataSets(
                cubicInterpolationMode = InterpolationMode.MONOTONE,
                data = dataBagList,
                fill = false,
                borderColor = dataBagList.mapIndexed { i, _ ->
                    chartColorList[i]
                },
                tension = 0.4
            )


        return p {
            chart(
                Configuration(
                    ChartType.LINE,
                    listOf(myDataSets),
                    labels
                )
            )
        }
    }


}

fun main() {
    startApplication(::App, module.hot, BootstrapModule, BootstrapCssModule, CoreModule)
}