package com.acme.transport.linehaul.laneplan.bundle

import com.acme.transport.kit.collections.groupBy
import com.acme.transport.kit.collections.sortedBy
import com.acme.transport.kit.collections.sumBy
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.numbers.percent
import com.acme.transport.kit.table.newTable
import com.acme.transport.linehaul.clock.Clock
import kotlin.math.round

/** Totals of weighted lane plan targets by name. */
data class WeightedLanePlanTargetReport(
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
        val table = newTable(title, mutableListOf<String>("name", "count", "total", "share"))
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

fun buildWeightedLanePlanTargetReport(title: String, items: MutableList<WeightedLanePlanTarget>, clock: Clock): WeightedLanePlanTargetReport {
    val groups = groupBy(items) { entry: WeightedLanePlanTarget -> entry.groupKey() }
    val totals = mutableMapOf<String, Double>()
    val counts = mutableMapOf<String, Int>()
    for ((key, members) in groups.toSortedMap()) {
        totals[key] = round(sumBy(members) { entry: WeightedLanePlanTarget -> entry.measuredValue() } * 100) / 100
        counts[key] = members.size
    }
    return WeightedLanePlanTargetReport(
        title = title,
        totals = totals,
        counts = counts,
        generatedAt = clock.now(),
    )
}
