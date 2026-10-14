package com.acme.transport.freightpay.audit.auditreview

import com.acme.transport.kit.text.normalize

/** A rule a adaptive audit variance must pass. Check returns its problems, or none. */
interface AdaptiveAuditVarianceRule {
    fun check(item: AdaptiveAuditVariance): MutableList<String>
}

data class AdaptiveAuditVarianceRequiredTitleRule(var message: String) : AdaptiveAuditVarianceRule {
    override fun check(item: AdaptiveAuditVariance): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveAuditVarianceRequiredRegionRule(var message: String) : AdaptiveAuditVarianceRule {
    override fun check(item: AdaptiveAuditVariance): MutableList<String> {
        if (item.region.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveAuditVarianceAllowedTitleRule(var allowed: MutableList<String>) : AdaptiveAuditVarianceRule {
    override fun check(item: AdaptiveAuditVariance): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is stale")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive audit variance. */
data class AdaptiveAuditVarianceValidator(var rules: MutableList<AdaptiveAuditVarianceRule>) {
    fun validate(item: AdaptiveAuditVariance): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AdaptiveAuditVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveAuditVariance>): MutableMap<String, MutableList<String>> {
        val report = mutableMapOf<String, MutableList<String>>()
        for (item in items) {
            val problems = validate(item)
            if (problems.size > 0) {
                report[item.code] = problems
            }
        }
        return report
    }
}

fun defaultAdaptiveAuditVarianceValidator(): AdaptiveAuditVarianceValidator {
    return AdaptiveAuditVarianceValidator(
        mutableListOf<AdaptiveAuditVarianceRule>(
            AdaptiveAuditVarianceRequiredTitleRule("adaptive audit variance needs this field"),
            AdaptiveAuditVarianceRequiredRegionRule("adaptive audit variance needs a value"),
            AdaptiveAuditVarianceAllowedTitleRule(mutableListOf<String>("global", "legacy", "tiered")),
        ),
    )
}
