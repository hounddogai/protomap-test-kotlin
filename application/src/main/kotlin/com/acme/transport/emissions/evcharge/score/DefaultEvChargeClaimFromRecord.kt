package com.acme.transport.emissions.evcharge.score

import com.acme.transport.emissions.carbonreport.agreement.CarbonReportRecord
import com.acme.transport.emissions.carbonreport.agreement.carbonReportRecordBucket

fun summarizeCarbonReportRecordsForClaim(items: MutableList<CarbonReportRecord>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netVolume()
    }
    return totals
}

fun deriveDefaultEvChargeClaimsFromRecord(items: MutableList<CarbonReportRecord>): MutableList<DefaultEvChargeClaim> {
    val out = mutableListOf<DefaultEvChargeClaim>()
    for (item in items) {
        val derived = newDefaultEvChargeClaim(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun planDefaultEvChargeClaim(item: DefaultEvChargeClaim): String = carbonReportRecordBucket(item.effectiveTotal())
