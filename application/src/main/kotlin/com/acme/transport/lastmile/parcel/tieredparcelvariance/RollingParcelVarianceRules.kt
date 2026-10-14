package com.acme.transport.lastmile.parcel.tieredparcelvariance

/** A rule a rolling parcel variance must pass. Check returns its problems, or none. */
interface RollingParcelVarianceRule {
    fun check(item: RollingParcelVariance): MutableList<String>
}

data class RollingParcelVarianceAttemptsRangeRule(var min: Int, var max: Int) : RollingParcelVarianceRule {
    override fun check(item: RollingParcelVariance): MutableList<String> {
        val value = item.attempts
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("attempts $value is below $min")
        }
        if (value > max) {
            problems.add("attempts $value is above $max")
        }
        return problems
    }
}

data class RollingParcelVarianceCodePrefixRule(var prefix: String) : RollingParcelVarianceRule {
    override fun check(item: RollingParcelVariance): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RollingParcelVarianceCreatedAtRangeRule(var min: Int, var max: Int) : RollingParcelVarianceRule {
    override fun check(item: RollingParcelVariance): MutableList<String> {
        val value = item.createdAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("created at $value is below $min")
        }
        if (value > max) {
            problems.add("created at $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a rolling parcel variance. */
data class RollingParcelVarianceValidator(var rules: MutableList<RollingParcelVarianceRule>) {
    fun validate(item: RollingParcelVariance): MutableList<String> {
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

    fun requireValid(item: RollingParcelVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingParcelVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingParcelVarianceValidator(): RollingParcelVarianceValidator {
    return RollingParcelVarianceValidator(
        mutableListOf<RollingParcelVarianceRule>(
            RollingParcelVarianceAttemptsRangeRule(120, 86400),
            RollingParcelVarianceCodePrefixRule("ACM"),
            RollingParcelVarianceCreatedAtRangeRule(3600, 86400),
        ),
    )
}
