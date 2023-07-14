package io.github.muqhc

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import io.kvision.*
import io.kvision.chart.*
import io.kvision.chart.js.Point
import io.kvision.chart.js.ScatterController
import io.kvision.chart.js.ScatterDataPoint
import io.kvision.core.*
import io.kvision.html.*
import io.kvision.panel.root
import io.kvision.panel.vPanel
import org.w3c.files.FileReader

class App : Application() {
    companion object {
        val rootId = "root"
        val chartId = "myChart"

        val chartColorList = (listOf(
            Col.BLUE,
            Col.RED,
            Col.BROWN,
            Col.HOTPINK,
            Col.CYAN,
            Col.GREEN,
            Col.PURPLE,
            Col.GOLD,
            Col.ORANGE
        ) + Col.values().toList()).map {
            Color.name(it)
        }
    }

    var chart: Component? = null

    override fun start() {
        root(rootId) {
            vPanel {
                input(InputType.FILE) {
                    onChange {
                        val file = getElementD().files[0]
                        val fr = FileReader()
                        fr.onload = {
                            val text = it.target.asDynamic().result

                            if (text != null) {
                                chart?.let { it1 -> this@vPanel.remove(it1) }
                                chart = null
                                chart = this@vPanel.myChart(text)
                            }

                            Unit
                        }
                        fr.readAsText(file)
                    }
                }
                button("make sample chart") {
                    onClick {
                        val text = sample
                        chart?.let { it1 -> this@vPanel.remove(it1) }
                        chart = null
                        chart = this@vPanel.myChart(text)
                    }
                }
            }
        }
//
    }

    fun Container.myChart(text: String): Component {
        val read = csvReader().readAll(text)
        val transformed = read[0].mapIndexed { i,_ ->
            read.map { it[i] }
        }
        val labels = transformed[0].drop(1)
        val dataBagTags = transformed.drop(1).map { it[0] }
        val dataBagList = transformed.drop(1).map {
            it.drop(1).map { it.toDoubleOrNull() ?: Double.NaN }
        }
        transformed.forEach { println(it) }

        val myDataSetsList =
            dataBagList.mapIndexed { i, it ->
                DataSets(
                    cubicInterpolationMode = InterpolationMode.MONOTONE,
                    data = it,
                    fill = false,
                    borderColor = listOf(chartColorList[i]),
                    tension = 0.4,
                    label = dataBagTags[i]
                )
            }

        val zipped = dataBagList.map { labels zip it }
        val combined = zipped.flatMapIndexed { i, l ->
            zipped.mapIndexed { j, it ->
                if (l == it) null
                else (i to j) to (l to it)
            }.filterNotNull()
        }
        val pointBagList = combined.map { (ij,lm) ->
            val (i,j) = ij
            val (l,m) = lm

            val pointList = l.flatMap { (s1,u) ->
                m.filter { (s2,_) -> s1 == s2 }.map { (_,v) -> u to v }
            }.distinct().filter {
                !it.first.isNaN() && !it.second.isNaN()
            }.map {
                object : ScatterDataPoint {
                    override var x: Number = it.first
                    override var y: Number = it.second
                }
            }

            val originDataSet = DataSets(
                data = pointList,
                pointBorderColor = listOf(i,j).map { chartColorList[it] },
                label = listOf(i,j).joinToString("-") {
                    dataBagTags[it]
                },
                pointBackgroundColor = listOf(i,j).map { chartColorList[it] },
                xAxisID = dataBagTags[i],
                yAxisID = dataBagTags[j]
            )
            i to originDataSet
        }
        val pointBagMatrix = pointBagList
            .groupBy { it.first }
            .map { it.value.map { it.second } }


        return hPanel {
            chart(
                Configuration(
                    ChartType.LINE,
                    myDataSetsList,
                    labels
                )
            )
            pointBagMatrix.forEach { l ->
                vPanel {
                    l.forEach {
                        chart(
                            Configuration(
                                ChartType.SCATTER,
                                listOf(it)
                            ) ,400,300
                        )
                    }
                }
            }
        }
    }


}

fun main() {
    startApplication(::App, module.hot, BootstrapModule, BootstrapCssModule, CoreModule)
}