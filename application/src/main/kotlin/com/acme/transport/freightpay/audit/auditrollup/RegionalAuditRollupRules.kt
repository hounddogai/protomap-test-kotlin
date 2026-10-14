package com.acme.transport.freightpay.audit.auditrollup

/** A rule a regional audit rollup must pass. Check returns its problems, or none. */
interface RegionalAuditRollupRule {
    fun check(item: RegionalAuditRollup): MutableList<String>
}

data class RegionalAuditRollupBudgetRangeRule(var min: Double, var max: Double) : RegionalAuditRollupRule {
    override fun check(item: RegionalAuditRollup): MutableList<String> {
        val value = item.budget
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("budget $value is below $min")
        }
        if (value > max) {
            problems.add("budget $value is above $max")
        }
        return problems
    }
}

data class RegionalAuditRollupCodePrefixRule(var prefix: String) : RegionalAuditRollupRule {
    override fun check(item: RegionalAuditRollup): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RegionalAuditRollupExpiresAtRangeRule(var min: Int, var max: Int) : RegionalAuditRollupRule {
    override fun check(item: RegionalAuditRollup): MutableList<String> {
        val value = item.expiresAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("expires at $value is below $min")
        }
        if (value > max) {
            problems.add("expires at $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a regional audit rollup. */
data class RegionalAuditRollupValidator(var rules: MutableList<RegionalAuditRollupRule>) {
    fun validate(item: RegionalAuditRollup): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            val found = rule.check(item)
            if (found.size == 0) {
                continue
            }
            for (problem in found) {
                problems.add("${item.code}: $problem")
            }
        }
        return problems
    }

    fun requireValid(item: RegionalAuditRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalAuditRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalAuditRollupValidator(): RegionalAuditRollupValidator {
    return RegionalAuditRollupValidator(
        mutableListOf<RegionalAuditRollupRule>(
            RegionalAuditRollupBudgetRangeRule(5.8, 3167.7),
            RegionalAuditRollupCodePrefixRule("Q"),
            RegionalAuditRollupExpiresAtRangeRule(80, 255),
        ),
    )
}
