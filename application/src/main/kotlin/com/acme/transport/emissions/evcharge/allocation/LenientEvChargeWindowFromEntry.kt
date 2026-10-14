package com.acme.transport.emissions.evcharge.allocation

import com.acme.transport.emissions.carbonreport.reportadjustment.CarbonReportEntry
import com.acme.transport.emissions.carbonreport.reportadjustment.carbonReportEntryBucket

fun summarizeCarbonReportEntriesForWindow(items: MutableList<CarbonReportEntry>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveLenientEvChargeWindowsFromEntry(items: MutableList<CarbonReportEntry>): MutableList<LenientEvChargeWindow> {
    val out = mutableListOf<LenientEvChargeWindow>()
    for (item in items) {
        val derived = newLenientEvChargeWindow(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun matchLenientEvChargeWindow(item: LenientEvChargeWindow): String {
    return carbonReportEntryBucket(item.measuredValue())
}
