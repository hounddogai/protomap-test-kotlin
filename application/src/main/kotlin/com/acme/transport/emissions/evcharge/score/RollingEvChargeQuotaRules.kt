package com.acme.transport.emissions.evcharge.score

/** A rule a rolling ev charge quota must pass. Check returns its problems, or none. */
interface RollingEvChargeQuotaRule {
    fun check(item: RollingEvChargeQuota): MutableList<String>
}

data class RollingEvChargeQuotaDiscountRangeRule(var min: Double, var max: Double) : RollingEvChargeQuotaRule {
    override fun check(item: RollingEvChargeQuota): MutableList<String> {
        val value = item.discount
        if (value < min || value > max) {
            return mutableListOf<String>("discount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RollingEvChargeQuotaThresholdRangeRule(var min: Double, var max: Double) : RollingEvChargeQuotaRule {
    override fun check(item: RollingEvChargeQuota): MutableList<String> {
        val value = item.threshold
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("threshold $value is below $min")
        }
        if (value > max) {
            problems.add("threshold $value is above $max")
        }
        return problems
    }
}

data class RollingEvChargeQuotaRequiredCodeRule(var message: String) : RollingEvChargeQuotaRule {
    override fun check(item: RollingEvChargeQuota): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingEvChargeQuotaTrailerIdLengthRule(var maxLength: Int) : RollingEvChargeQuotaRule {
    override fun check(item: RollingEvChargeQuota): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingEvChargeQuotaCeilingRangeRule(var min: Double, var max: Double) : RollingEvChargeQuotaRule {
    override fun check(item: RollingEvChargeQuota): MutableList<String> {
        val value = item.ceiling
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ceiling $value is below $min")
        }
        if (value > max) {
            problems.add("ceiling $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a rolling ev charge quota. */
data class RollingEvChargeQuotaValidator(var rules: MutableList<RollingEvChargeQuotaRule>) {
    fun validate(item: RollingEvChargeQuota): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RollingEvChargeQuota) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingEvChargeQuota>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingEvChargeQuotaValidator(): RollingEvChargeQuotaValidator {
    return RollingEvChargeQuotaValidator(
        mutableListOf<RollingEvChargeQuotaRule>(
            RollingEvChargeQuotaDiscountRangeRule(1.6, 2030.4),
            RollingEvChargeQuotaThresholdRangeRule(0.2, 3635.3),
            RollingEvChargeQuotaRequiredCodeRule("rolling ev charge quota needs this field"),
            RollingEvChargeQuotaTrailerIdLengthRule(80),
            RollingEvChargeQuotaCeilingRangeRule(0.6, 2933.5),
        ),
    )
}
