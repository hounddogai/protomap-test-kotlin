package com.acme.transport.safety.nearmiss.variance

/** A rule a default near miss metric must pass. Check returns its problems, or none. */
interface DefaultNearMissMetricRule {
    fun check(item: DefaultNearMissMetric): MutableList<String>
}

data class DefaultNearMissMetricRequiredCodeRule(var message: String) : DefaultNearMissMetricRule {
    override fun check(item: DefaultNearMissMetric): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultNearMissMetricCeilingRangeRule(var min: Double, var max: Double) : DefaultNearMissMetricRule {
    override fun check(item: DefaultNearMissMetric): MutableList<String> {
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

data class DefaultNearMissMetricRevisionRangeRule(var min: Int, var max: Int) : DefaultNearMissMetricRule {
    override fun check(item: DefaultNearMissMetric): MutableList<String> {
        val value = item.revision
        if (value < min || value > max) {
            return mutableListOf<String>("revision $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DefaultNearMissMetricChecksumLengthRule(var maxLength: Int) : DefaultNearMissMetricRule {
    override fun check(item: DefaultNearMissMetric): MutableList<String> {
        val size = item.checksum.length
        if (size > maxLength) {
            return mutableListOf<String>("checksum is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default near miss metric. */
data class DefaultNearMissMetricValidator(var rules: MutableList<DefaultNearMissMetricRule>) {
    fun validate(item: DefaultNearMissMetric): MutableList<String> {
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

    fun requireValid(item: DefaultNearMissMetric) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultNearMissMetric>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultNearMissMetricValidator(): DefaultNearMissMetricValidator {
    return DefaultNearMissMetricValidator(
        mutableListOf<DefaultNearMissMetricRule>(
            DefaultNearMissMetricRequiredCodeRule("default near miss metric needs an entry"),
            DefaultNearMissMetricCeilingRangeRule(0.2, 3429.4),
            DefaultNearMissMetricRevisionRangeRule(120, 3600),
            DefaultNearMissMetricChecksumLengthRule(16),
        ),
    )
}
