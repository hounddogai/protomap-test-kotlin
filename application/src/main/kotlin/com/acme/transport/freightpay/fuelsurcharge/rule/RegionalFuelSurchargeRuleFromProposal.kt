package com.acme.transport.freightpay.fuelsurcharge.rule

import com.acme.transport.freightpay.audit.auditpolicy.StrictAuditProposal
import com.acme.transport.freightpay.audit.auditpolicy.classifyLegacyAuditRule

fun summarizeStrictAuditProposalsForRule(items: MutableList<StrictAuditProposal>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedVolume()
    }
    return totals
}

fun deriveRegionalFuelSurchargeRulesFromProposal(items: MutableList<StrictAuditProposal>): MutableList<RegionalFuelSurchargeRule> {
    val out = mutableListOf<RegionalFuelSurchargeRule>()
    for (item in items) {
        val derived = newRegionalFuelSurchargeRule(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun classifyRegionalFuelSurchargeRuleExtra(item: RegionalFuelSurchargeRule): String {
    return classifyLegacyAuditRule(item.netTotal())
}
