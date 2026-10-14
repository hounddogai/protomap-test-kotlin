package com.acme.transport.fleetops.utilization.milestone

/** A rule a rolling utilization quota must pass. Check returns its problems, or none. */
interface RollingUtilizationQuotaRule {
    fun check(item: RollingUtilizationQuota): MutableList<String>
}

data class RollingUtilizationQuotaFuelLitersRangeRule(
    var min: Double,
    var max: Double,
) : RollingUtilizationQuotaRule {
    override fun check(item: RollingUtilizationQuota): MutableList<String> {
        val value = item.fuelLiters
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("fuel liters $value is below $min")
        }
        if (value > max) {
            problems.add("fuel liters $value is above $max")
        }
        return problems
    }
}

data class RollingUtilizationQuotaRequiredNameRule(var message: String) : RollingUtilizationQuotaRule {
    override fun check(item: RollingUtilizationQuota): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingUtilizationQuotaCodeLengthRule(var maxLength: Int) : RollingUtilizationQuotaRule {
    override fun check(item: RollingUtilizationQuota): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingUtilizationQuotaVisibleGuardRule(var limit: Int) : RollingUtilizationQuotaRule {
    override fun check(item: RollingUtilizationQuota): MutableList<String> {
        if (item.visible && item.limit > limit) {
            return mutableListOf<String>("limit ${item.limit} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling utilization quota. */
data class RollingUtilizationQuotaValidator(var rules: MutableList<RollingUtilizationQuotaRule>) {
    fun validate(item: RollingUtilizationQuota): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RollingUtilizationQuota) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingUtilizationQuota>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingUtilizationQuotaValidator(): RollingUtilizationQuotaValidator {
    return RollingUtilizationQuotaValidator(
        mutableListOf<RollingUtilizationQuotaRule>(
            RollingUtilizationQuotaFuelLitersRangeRule(0.8, 3936.5),
            RollingUtilizationQuotaRequiredNameRule("rolling utilization quota needs an entry"),
            RollingUtilizationQuotaCodeLengthRule(32),
            RollingUtilizationQuotaVisibleGuardRule(64),
        ),
    )
}
