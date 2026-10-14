package com.acme.transport.network.transittime.timethreshold

import com.acme.transport.kit.text.normalize

/** A rule a strict transit time audit must pass. Check returns its problems, or none. */
interface StrictTransitTimeAuditRule {
    fun check(item: StrictTransitTimeAudit): MutableList<String>
}

data class StrictTransitTimeAuditAllowedCodeRule(
    var allowed: MutableList<String>,
) : StrictTransitTimeAuditRule {
    override fun check(item: StrictTransitTimeAudit): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class StrictTransitTimeAuditRetriesRangeRule(var min: Int, var max: Int) : StrictTransitTimeAuditRule {
    override fun check(item: StrictTransitTimeAudit): MutableList<String> {
        val value = item.retries
        if (value < min || value > max) {
            return mutableListOf<String>("retries $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class StrictTransitTimeAuditRequiredNameRule(var message: String) : StrictTransitTimeAuditRule {
    override fun check(item: StrictTransitTimeAudit): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictTransitTimeAuditDiscountRangeRule(
    var min: Double,
    var max: Double,
) : StrictTransitTimeAuditRule {
    override fun check(item: StrictTransitTimeAudit): MutableList<String> {
        val value = item.discount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("discount $value is below $min")
        }
        if (value > max) {
            problems.add("discount $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a strict transit time audit. */
data class StrictTransitTimeAuditValidator(var rules: MutableList<StrictTransitTimeAuditRule>) {
    fun validate(item: StrictTransitTimeAudit): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: StrictTransitTimeAudit) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictTransitTimeAudit>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictTransitTimeAuditValidator(): StrictTransitTimeAuditValidator {
    return StrictTransitTimeAuditValidator(
        mutableListOf<StrictTransitTimeAuditRule>(
            StrictTransitTimeAuditAllowedCodeRule(mutableListOf<String>("scheduled")),
            StrictTransitTimeAuditRetriesRangeRule(3600, 86400),
            StrictTransitTimeAuditRequiredNameRule("strict transit time audit needs this field"),
            StrictTransitTimeAuditDiscountRangeRule(5.7, 326.3),
        ),
    )
}
