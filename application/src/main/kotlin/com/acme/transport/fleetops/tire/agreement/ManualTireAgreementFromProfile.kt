package com.acme.transport.fleetops.tire.agreement

import com.acme.transport.fleetops.leasereturn.profile.PrimaryLeaseReturnProfile
import com.acme.transport.fleetops.leasereturn.profile.rollingLeaseReturnGroupWeight

fun summarizePrimaryLeaseReturnProfilesForAgreement(items: MutableList<PrimaryLeaseReturnProfile>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedTotal()
    }
    return totals
}

fun deriveManualTireAgreementsFromProfile(items: MutableList<PrimaryLeaseReturnProfile>): MutableList<ManualTireAgreement> {
    val out = mutableListOf<ManualTireAgreement>()
    for (item in items) {
        val derived = newManualTireAgreement(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun splitManualTireAgreement(item: ManualTireAgreement): Double = rollingLeaseReturnGroupWeight(7, true)
