package com.acme.transport.safety.nearmiss.profile

import com.acme.transport.kit.collections.groupBy
import com.acme.transport.kit.collections.sortedBy
import com.acme.transport.kit.collections.sumBy
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.numbers.percent
import com.acme.transport.kit.table.newTable
import com.acme.transport.safety.clock.Clock
import kotlin.math.round

/** Totals of near miss profiles by region. */
data class NearMissProfileReport(
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

fun buildNearMissProfileReport(title: String, items: MutableList<NearMissProfile>, clock: Clock): NearMissProfileReport {
    val groups = groupBy(items) { entry: NearMissProfile -> entry.groupKey() }
    val totals = mutableMapOf<String, Double>()
    val counts = mutableMapOf<String, Int>()
    for ((key, members) in groups.toSortedMap()) {
        totals[key] = round(sumBy(members) { entry: NearMissProfile -> entry.netExposure() } * 100) / 100
        counts[key] = members.size
    }
    return NearMissProfileReport(title = title, totals = totals, counts = counts, generatedAt = clock.now())
}
