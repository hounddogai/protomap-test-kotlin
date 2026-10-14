package com.acme.transport.freightpay.dispute.group

import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.sortedBy
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.numbers.percent
import com.acme.transport.kit.table.newTable

/** Totals of dispute quotas by depot. */
data class DisputeQuotaReport(
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
        val table = newTable(title, mutableListOf<String>("depot", "count", "total", "share"))
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

fun buildDisputeQuotaReport(title: String, items: MutableList<DisputeQuota>, clock: Clock): DisputeQuotaReport {
    val totals = mutableMapOf<String, Double>()
    val counts = mutableMapOf<String, Int>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        val key = item.groupKey()
        totals[key] = (totals[key] ?: 0.0) + item.netTotal()
        counts[key] = (counts[key] ?: 0) + 1
    }
    return DisputeQuotaReport(title = title, totals = totals, counts = counts, generatedAt = clock.now())
}
