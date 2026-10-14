package com.acme.transport.safety.nearmiss.metric

import com.acme.transport.kit.collections.sortedBy
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.numbers.percent
import com.acme.transport.kit.table.newTable
import com.acme.transport.safety.clock.Clock

/** Totals of lenient near miss mappings by country. */
data class LenientNearMissMappingReport(
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
        val table = newTable(title, mutableListOf<String>("country", "count", "total", "share"))
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

fun buildLenientNearMissMappingReport(title: String, items: MutableList<LenientNearMissMapping>, clock: Clock): LenientNearMissMappingReport {
    val totals = mutableMapOf<String, Double>()
    val counts = mutableMapOf<String, Int>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        val key = item.groupKey()
        totals[key] = (totals[key] ?: 0.0) + item.effectiveExposure()
        counts[key] = (counts[key] ?: 0) + 1
    }
    return LenientNearMissMappingReport(
        title = title,
        totals = totals,
        counts = counts,
        generatedAt = clock.now(),
    )
}
