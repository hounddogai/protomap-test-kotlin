package com.acme.transport.lastmile.proofofdelivery.deliveryreview

import com.acme.transport.kit.collections.groupBy
import com.acme.transport.kit.collections.sortedBy
import com.acme.transport.kit.collections.sumBy
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.numbers.percent
import com.acme.transport.kit.table.newTable
import com.acme.transport.lastmile.clock.Clock
import kotlin.math.round

/** Totals of manual proof of delivery plans by title. */
data class ManualProofOfDeliveryPlanReport(
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
        val table = newTable(title, mutableListOf<String>("title", "count", "total", "share"))
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

fun buildManualProofOfDeliveryPlanReport(title: String, items: MutableList<ManualProofOfDeliveryPlan>, clock: Clock): ManualProofOfDeliveryPlanReport {
    val groups = groupBy(items) { entry: ManualProofOfDeliveryPlan -> entry.groupKey() }
    val totals = mutableMapOf<String, Double>()
    val counts = mutableMapOf<String, Int>()
    for ((key, members) in groups.toSortedMap()) {
        totals[key] = round(sumBy(members) { entry: ManualProofOfDeliveryPlan -> entry.adjustedExposure() } * 100) / 100
        counts[key] = members.size
    }
    return ManualProofOfDeliveryPlanReport(
        title = title,
        totals = totals,
        counts = counts,
        generatedAt = clock.now(),
    )
}
