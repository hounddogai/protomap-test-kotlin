package com.acme.transport.customs.broker.adjustment

/** A rule a regional broker audit must pass. Check returns its problems, or none. */
interface RegionalBrokerAuditRule {
    fun check(item: RegionalBrokerAudit): MutableList<String>
}

data class RegionalBrokerAuditUpdatedAtRangeRule(var min: Int, var max: Int) : RegionalBrokerAuditRule {
    override fun check(item: RegionalBrokerAudit): MutableList<String> {
        val value = item.updatedAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("updated at $value is below $min")
        }
        if (value > max) {
            problems.add("updated at $value is above $max")
        }
        return problems
    }
}

data class RegionalBrokerAuditCarrierLengthRule(var maxLength: Int) : RegionalBrokerAuditRule {
    override fun check(item: RegionalBrokerAudit): MutableList<String> {
        val size = item.carrier.length
        if (size > maxLength) {
            return mutableListOf<String>("carrier is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalBrokerAuditScoreRangeRule(var min: Double, var max: Double) : RegionalBrokerAuditRule {
    override fun check(item: RegionalBrokerAudit): MutableList<String> {
        val value = item.score
        if (value < min || value > max) {
            return mutableListOf<String>("score $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RegionalBrokerAuditPinnedGuardRule(var limit: Int) : RegionalBrokerAuditRule {
    override fun check(item: RegionalBrokerAudit): MutableList<String> {
        if (item.pinned && item.updatedAt > limit) {
            return mutableListOf<String>("updated at ${item.updatedAt} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

data class RegionalBrokerAuditRequiredCodeRule(var message: String) : RegionalBrokerAuditRule {
    override fun check(item: RegionalBrokerAudit): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional broker audit. */
data class RegionalBrokerAuditValidator(var rules: MutableList<RegionalBrokerAuditRule>) {
    fun validate(item: RegionalBrokerAudit): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RegionalBrokerAudit) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalBrokerAudit>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalBrokerAuditValidator(): RegionalBrokerAuditValidator {
    return RegionalBrokerAuditValidator(
        mutableListOf<RegionalBrokerAuditRule>(
            RegionalBrokerAuditUpdatedAtRangeRule(80, 1000),
            RegionalBrokerAuditCarrierLengthRule(80),
            RegionalBrokerAuditScoreRangeRule(1.9, 4903.9),
            RegionalBrokerAuditPinnedGuardRule(80),
            RegionalBrokerAuditRequiredCodeRule("regional broker audit needs an entry"),
        ),
    )
}
