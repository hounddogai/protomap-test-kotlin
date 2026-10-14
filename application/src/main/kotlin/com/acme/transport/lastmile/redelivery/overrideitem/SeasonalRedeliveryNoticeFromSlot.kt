package com.acme.transport.lastmile.redelivery.overrideitem

import com.acme.transport.lastmile.parcel.slot.RegionalParcelSlot
import com.acme.transport.lastmile.parcel.slot.normalizeRegionalParcelSlotCode

fun summarizeRegionalParcelSlotsForNotice(items: MutableList<RegionalParcelSlot>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveSeasonalRedeliveryNoticesFromSlot(items: MutableList<RegionalParcelSlot>): MutableList<SeasonalRedeliveryNotice> {
    val out = mutableListOf<SeasonalRedeliveryNotice>()
    for (item in items) {
        val derived = newSeasonalRedeliveryNotice(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun planSeasonalRedeliveryNotice(item: SeasonalRedeliveryNotice): String {
    return normalizeRegionalParcelSlotCode(item.code)
}
