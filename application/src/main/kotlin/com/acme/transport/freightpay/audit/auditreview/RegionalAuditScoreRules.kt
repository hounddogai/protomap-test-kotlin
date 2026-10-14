package com.acme.transport.freightpay.audit.auditreview

import com.acme.transport.kit.text.normalize

/** A rule a regional audit score must pass. Check returns its problems, or none. */
interface RegionalAuditScoreRule {
    fun check(item: RegionalAuditScore): MutableList<String>
}

data class RegionalAuditScoreRequiredCodeRule(var message: String) : RegionalAuditScoreRule {
    override fun check(item: RegionalAuditScore): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalAuditScoreAttemptsRangeRule(var min: Int, var max: Int) : RegionalAuditScoreRule {
    override fun check(item: RegionalAuditScore): MutableList<String> {
        val value = item.attempts
        if (value < min || value > max) {
            return mutableListOf<String>("attempts $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RegionalAuditScoreRequiredLabelRule(var message: String) : RegionalAuditScoreRule {
    override fun check(item: RegionalAuditScore): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalAuditScoreQuantityRangeRule(var min: Int, var max: Int) : RegionalAuditScoreRule {
    override fun check(item: RegionalAuditScore): MutableList<String> {
        val value = item.quantity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("quantity $value is below $min")
        }
        if (value > max) {
            problems.add("quantity $value is above $max")
        }
        return problems
    }
}

data class RegionalAuditScoreAllowedDepotRule(var allowed: MutableList<String>) : RegionalAuditScoreRule {
    override fun check(item: RegionalAuditScore): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.depot))) {
            return mutableListOf<String>("depot ${item.depot} is out of range")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional audit score. */
data class RegionalAuditScoreValidator(var rules: MutableList<RegionalAuditScoreRule>) {
    fun validate(item: RegionalAuditScore): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RegionalAuditScore) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalAuditScore>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalAuditScoreValidator(): RegionalAuditScoreValidator {
    return RegionalAuditScoreValidator(
        mutableListOf<RegionalAuditScoreRule>(
            RegionalAuditScoreRequiredCodeRule("regional audit score needs this field"),
            RegionalAuditScoreAttemptsRangeRule(16, 255),
            RegionalAuditScoreRequiredLabelRule("regional audit score needs an entry"),
            RegionalAuditScoreQuantityRangeRule(120, 255),
            RegionalAuditScoreAllowedDepotRule(mutableListOf<String>("fallback", "primary")),
        ),
    )
}
