package com.acme.transport.freightpay.fuelsurcharge.surchargebaseline

import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.groupBy
import com.acme.transport.kit.collections.sortedBy
import com.acme.transport.kit.collections.sumBy
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.numbers.percent
import com.acme.transport.kit.table.newTable
import kotlin.math.round

/** Totals of tiered fuel surcharge baselines by unit. */
data class TieredFuelSurchargeBaselineReport(
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
        val table = newTable(title, mutableListOf<String>("unit", "count", "total", "share"))
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

fun buildTieredFuelSurchargeBaselineReport(title: String, items: MutableList<TieredFuelSurchargeBaseline>, clock: Clock): TieredFuelSurchargeBaselineReport {
    val groups = groupBy(items) { entry: TieredFuelSurchargeBaseline -> entry.groupKey() }
    val totals = mutableMapOf<String, Double>()
    val counts = mutableMapOf<String, Int>()
    for ((key, members) in groups.toSortedMap()) {
        totals[key] = round(sumBy(members) { entry: TieredFuelSurchargeBaseline -> entry.netVolume() } * 100) / 100
        counts[key] = members.size
    }
    return TieredFuelSurchargeBaselineReport(
        title = title,
        totals = totals,
        counts = counts,
        generatedAt = clock.now(),
    )
}
