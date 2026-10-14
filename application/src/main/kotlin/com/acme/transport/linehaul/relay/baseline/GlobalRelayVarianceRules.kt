package com.acme.transport.linehaul.relay.baseline

/** A rule a global relay variance must pass. Check returns its problems, or none. */
interface GlobalRelayVarianceRule {
    fun check(item: GlobalRelayVariance): MutableList<String>
}

data class GlobalRelayVarianceLabelLengthRule(var maxLength: Int) : GlobalRelayVarianceRule {
    override fun check(item: GlobalRelayVariance): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalRelayVarianceThresholdRangeRule(var min: Double, var max: Double) : GlobalRelayVarianceRule {
    override fun check(item: GlobalRelayVariance): MutableList<String> {
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

data class GlobalRelayVarianceCodeLengthRule(var maxLength: Int) : GlobalRelayVarianceRule {
    override fun check(item: GlobalRelayVariance): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalRelayVarianceCostRangeRule(var min: Double, var max: Double) : GlobalRelayVarianceRule {
    override fun check(item: GlobalRelayVariance): MutableList<String> {
        val value = item.cost
        if (value < min || value > max) {
            return mutableListOf<String>("cost $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global relay variance. */
data class GlobalRelayVarianceValidator(var rules: MutableList<GlobalRelayVarianceRule>) {
    fun validate(item: GlobalRelayVariance): MutableList<String> {
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

    fun requireValid(item: GlobalRelayVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalRelayVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalRelayVarianceValidator(): GlobalRelayVarianceValidator {
    return GlobalRelayVarianceValidator(
        mutableListOf<GlobalRelayVarianceRule>(
            GlobalRelayVarianceLabelLengthRule(16),
            GlobalRelayVarianceThresholdRangeRule(1.7, 286.5),
            GlobalRelayVarianceCodeLengthRule(64),
            GlobalRelayVarianceCostRangeRule(5.2, 4272.6),
        ),
    )
}
