package com.acme.transport.safety.coaching.tier

/** A rule a default coaching metric must pass. Check returns its problems, or none. */
interface DefaultCoachingMetricRule {
    fun check(item: DefaultCoachingMetric): MutableList<String>
}

data class DefaultCoachingMetricRequiredUnitRule(var message: String) : DefaultCoachingMetricRule {
    override fun check(item: DefaultCoachingMetric): MutableList<String> {
        if (item.unit.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultCoachingMetricRequiredChecksumRule(var message: String) : DefaultCoachingMetricRule {
    override fun check(item: DefaultCoachingMetric): MutableList<String> {
        if (item.checksum.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultCoachingMetricChecksumLengthRule(var maxLength: Int) : DefaultCoachingMetricRule {
    override fun check(item: DefaultCoachingMetric): MutableList<String> {
        val size = item.checksum.length
        if (size > maxLength) {
            return mutableListOf<String>("checksum is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultCoachingMetricCodeLengthRule(var maxLength: Int) : DefaultCoachingMetricRule {
    override fun check(item: DefaultCoachingMetric): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultCoachingMetricVersionRangeRule(var min: Int, var max: Int) : DefaultCoachingMetricRule {
    override fun check(item: DefaultCoachingMetric): MutableList<String> {
        val value = item.version
        if (value < min || value > max) {
            return mutableListOf<String>("version $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DefaultCoachingMetricNameLengthRule(var maxLength: Int) : DefaultCoachingMetricRule {
    override fun check(item: DefaultCoachingMetric): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default coaching metric. */
data class DefaultCoachingMetricValidator(var rules: MutableList<DefaultCoachingMetricRule>) {
    fun validate(item: DefaultCoachingMetric): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefaultCoachingMetric) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultCoachingMetric>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultCoachingMetricValidator(): DefaultCoachingMetricValidator {
    return DefaultCoachingMetricValidator(
        mutableListOf<DefaultCoachingMetricRule>(
            DefaultCoachingMetricRequiredUnitRule("default coaching metric needs a value"),
            DefaultCoachingMetricRequiredChecksumRule("default coaching metric needs an entry"),
            DefaultCoachingMetricChecksumLengthRule(3600),
            DefaultCoachingMetricCodeLengthRule(80),
            DefaultCoachingMetricVersionRangeRule(16, 120),
            DefaultCoachingMetricNameLengthRule(1000),
        ),
    )
}
