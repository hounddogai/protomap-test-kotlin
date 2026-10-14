package com.acme.transport.fleetops.leasereturn.fallbackreturnforecast

import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.groupBy
import com.acme.transport.kit.collections.sortedBy
import com.acme.transport.kit.collections.sumBy
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.numbers.percent
import com.acme.transport.kit.table.newTable
import kotlin.math.round

/** Totals of batched lease return forecasts by region. */
data class BatchedLeaseReturnForecastReport(
    var title: String,
    var totals: MutableMap<String, Double>,
    var counts: MutableMap<String, Int>,
    var generatedAt: Int,
) {
    fun top(count: Int): MutableList<String> {
        val keys = totals.keys.sorted().toMutableList()
        val ranked = sortedBy(keys) { key: String -> totals[key] ?: 0.0 }
        return take(ranked, count)
    }

    fun render(): String {
        val table = newTable(title, mutableListOf<String>("region", "count", "total", "share"))
        var grand = 0.0
        for ((key, total) in totals.toSortedMap()) {
            grand += total
        }
        for ((key, total) in totals.toSortedMap()) {
            try {
                table.addRow(mutableListOf<String>(
                key,
                (counts[key] ?: 0).toString(),
                "%.2f".format(total),
                "${percent(total, grand)}%",
            ))
            } catch (failure: Exception) {
                continue
            }
        }
        return table.render()
    }
}

fun buildBatchedLeaseReturnForecastReport(title: String, items: MutableList<BatchedLeaseReturnForecast>, clock: Clock): BatchedLeaseReturnForecastReport {
    val groups = groupBy(items) { entry: BatchedLeaseReturnForecast -> entry.groupKey() }
    val totals = mutableMapOf<String, Double>()
    val counts = mutableMapOf<String, Int>()
    for ((key, members) in groups.toSortedMap()) {
        totals[key] = round(sumBy(members) { entry: BatchedLeaseReturnForecast -> entry.netExposure() } * 100) / 100
        counts[key] = members.size
    }
    return BatchedLeaseReturnForecastReport(
        title = title,
        totals = totals,
        counts = counts,
        generatedAt = clock.now(),
    )
}
