package com.oic.myapplication.ui.screens.reporting

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.core.entry.entryModelOf
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.oic.myapplication.services.database.models.DailyLog
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import com.oic.myapplication.ui.palette.*

class Charts {

    companion object {

        /**
         * Generates a weekly litres chart from daily logs.
         * @param dailyLogs List of DailyLog entries.
         * @param modifier Modifier for styling the Surface.
         */
        @Composable
        fun WeeklyLitresChart(dailyLogs: List<DailyLog>, modifier: Modifier = Modifier) {
            if (dailyLogs.isEmpty()) return

            // Aggregate weekly litres
            val weekMap = mutableMapOf<LocalDate, Int>()
            dailyLogs.forEach { day ->
                val date = LocalDate.parse(day.date)
                val weekStart = date.minusDays(date.dayOfWeek.value.toLong() - 1)
                val total = day.logs.values.sumOf { it.litres }
                weekMap[weekStart] = (weekMap[weekStart] ?: 0) + total
            }
            val sortedWeeks = weekMap.toSortedMap()
            val weekDates = sortedWeeks.keys.toList()
            val entries = entryModelOf(*sortedWeeks.values.mapIndexed { idx, litres ->
                idx.toFloat() to litres.toFloat()
            }.toTypedArray())

            Surface(
                shape = CardXL,
                color = SurfaceWhite,
                tonalElevation = 1.dp,
                modifier = modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Text(
                        text = "Weekly Litres",
                        color = CocoaDeep,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(Modifier.height(8.dp))
                    Chart(
                        chart = lineChart(),
                        model = entries,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        startAxis = rememberStartAxis(
                            valueFormatter = AxisValueFormatter { value, _ -> "${value.toInt()} L" }
                        ),
                        bottomAxis = rememberBottomAxis(
                            valueFormatter = AxisValueFormatter { value, _ ->
                                val idx = value.toInt()
                                if (idx in weekDates.indices) weekDates[idx].format(DateTimeFormatter.ofPattern("dd/MM")) else ""
                            }
                        )
                    )
                }
            }
        }

        /**
         * Generates a monthly litres chart from daily logs.
         * @param dailyLogs List of DailyLog entries.
         * @param modifier Modifier for styling the Surface.
         */
        @Composable
        fun MonthlyLitresChart(dailyLogs: List<DailyLog>, modifier: Modifier = Modifier) {
            if (dailyLogs.isEmpty()) return

            // Aggregate monthly litres
            val monthMap = mutableMapOf<LocalDate, Int>()
            dailyLogs.forEach { day ->
                val date = LocalDate.parse(day.date)
                val monthStart = date.withDayOfMonth(1)
                val total = day.logs.values.sumOf { it.litres }
                monthMap[monthStart] = (monthMap[monthStart] ?: 0) + total
            }
            val sortedMonths = monthMap.toSortedMap()
            val monthDates = sortedMonths.keys.toList()
            val entries = entryModelOf(*sortedMonths.values.mapIndexed { idx, litres ->
                idx.toFloat() to litres.toFloat()
            }.toTypedArray())

            Surface(
                shape = CardXL,
                color = SurfaceWhite,
                tonalElevation = 1.dp,
                modifier = modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Text(
                        text = "Monthly Litres",
                        color = CocoaDeep,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(Modifier.height(8.dp))
                    Chart(
                        chart = lineChart(),
                        model = entries,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        startAxis = rememberStartAxis(
                            valueFormatter = AxisValueFormatter { value, _ -> "${value.toInt()} L" }
                        ),
                        bottomAxis = rememberBottomAxis(
                            valueFormatter = AxisValueFormatter { value, _ ->
                                val idx = value.toInt()
                                if (idx in monthDates.indices) monthDates[idx].format(DateTimeFormatter.ofPattern("MMM yyyy")) else ""
                            }
                        )
                    )
                }
            }
        }
    }
}
