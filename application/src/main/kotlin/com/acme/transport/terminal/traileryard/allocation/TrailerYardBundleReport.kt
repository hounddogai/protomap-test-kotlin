package com.acme.transport.terminal.traileryard.allocation

import com.acme.transport.kit.collections.sortedBy
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.numbers.percent
import com.acme.transport.kit.table.newTable
import com.acme.transport.terminal.clock.Clock

/** Totals of trailer yard bundles by source. */
data class TrailerYardBundleReport(
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
        val table = newTable(title, mutableListOf<String>("source", "count", "total", "share"))
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

fun buildTrailerYardBundleReport(title: String, items: MutableList<TrailerYardBundle>, clock: Clock): TrailerYardBundleReport {
    val totals = mutableMapOf<String, Double>()
    val counts = mutableMapOf<String, Int>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        val key = item.groupKey()
        totals[key] = (totals[key] ?: 0.0) + item.projectedLoad()
        counts[key] = (counts[key] ?: 0) + 1
    }
    return TrailerYardBundleReport(title = title, totals = totals, counts = counts, generatedAt = clock.now())
}
