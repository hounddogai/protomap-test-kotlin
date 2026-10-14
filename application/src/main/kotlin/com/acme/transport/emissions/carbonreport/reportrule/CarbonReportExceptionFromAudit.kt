package com.acme.transport.emissions.carbonreport.reportrule

import com.acme.transport.emissions.fuelburn.audit.FuelBurnAudit
import com.acme.transport.emissions.fuelburn.audit.fuelBurnAssignmentWeight

fun summarizeFuelBurnAuditsForException(items: MutableList<FuelBurnAudit>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netLoad()
    }
    return totals
}

fun deriveCarbonReportExceptionsFromAudit(items: MutableList<FuelBurnAudit>): MutableList<CarbonReportException> {
    val out = mutableListOf<CarbonReportException>()
    for (item in items) {
        val derived = newCarbonReportException(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun rankCarbonReportException(item: CarbonReportException): Double = fuelBurnAssignmentWeight(5, true)
