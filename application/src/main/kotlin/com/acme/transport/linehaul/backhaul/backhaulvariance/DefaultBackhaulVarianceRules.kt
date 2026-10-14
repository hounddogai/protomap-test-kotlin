package com.acme.transport.linehaul.backhaul.backhaulvariance

/** A rule a default backhaul variance must pass. Check returns its problems, or none. */
interface DefaultBackhaulVarianceRule {
    fun check(item: DefaultBackhaulVariance): MutableList<String>
}

data class DefaultBackhaulVarianceCodePrefixRule(var prefix: String) : DefaultBackhaulVarianceRule {
    override fun check(item: DefaultBackhaulVariance): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DefaultBackhaulVarianceThresholdRangeRule(
    var min: Double,
    var max: Double,
) : DefaultBackhaulVarianceRule {
    override fun check(item: DefaultBackhaulVariance): MutableList<String> {
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

data class DefaultBackhaulVarianceActiveGuardRule(var limit: Double) : DefaultBackhaulVarianceRule {
    override fun check(item: DefaultBackhaulVariance): MutableList<String> {
        if (item.active && item.threshold > limit) {
            return mutableListOf<String>("threshold ${item.threshold} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default backhaul variance. */
data class DefaultBackhaulVarianceValidator(var rules: MutableList<DefaultBackhaulVarianceRule>) {
    fun validate(item: DefaultBackhaulVariance): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefaultBackhaulVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultBackhaulVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultBackhaulVarianceValidator(): DefaultBackhaulVarianceValidator {
    return DefaultBackhaulVarianceValidator(
        mutableListOf<DefaultBackhaulVarianceRule>(
            DefaultBackhaulVarianceCodePrefixRule("OPS"),
            DefaultBackhaulVarianceThresholdRangeRule(7.0, 2160.4),
            DefaultBackhaulVarianceActiveGuardRule(6.6),
        ),
    )
}
