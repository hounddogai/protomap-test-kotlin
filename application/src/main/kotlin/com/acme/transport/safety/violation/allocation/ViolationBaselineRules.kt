package com.acme.transport.safety.violation.allocation

/** A rule a violation baseline must pass. Check returns its problems, or none. */
interface ViolationBaselineRule {
    fun check(item: ViolationBaseline): MutableList<String>
}

data class ViolationBaselineRequiredChannelRule(var message: String) : ViolationBaselineRule {
    override fun check(item: ViolationBaseline): MutableList<String> {
        if (item.channel.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ViolationBaselineCostRangeRule(var min: Double, var max: Double) : ViolationBaselineRule {
    override fun check(item: ViolationBaseline): MutableList<String> {
        val value = item.cost
        if (value < min || value > max) {
            return mutableListOf<String>("cost $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ViolationBaselineLabelLengthRule(var maxLength: Int) : ViolationBaselineRule {
    override fun check(item: ViolationBaseline): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ViolationBaselineCodePrefixRule(var prefix: String) : ViolationBaselineRule {
    override fun check(item: ViolationBaseline): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a violation baseline. */
data class ViolationBaselineValidator(var rules: MutableList<ViolationBaselineRule>) {
    fun validate(item: ViolationBaseline): MutableList<String> {
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

    fun requireValid(item: ViolationBaseline) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ViolationBaseline>): MutableMap<String, MutableList<String>> {
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

fun defaultViolationBaselineValidator(): ViolationBaselineValidator {
    return ViolationBaselineValidator(
        mutableListOf<ViolationBaselineRule>(
            ViolationBaselineRequiredChannelRule("violation baseline needs an entry"),
            ViolationBaselineCostRangeRule(1.2, 1756.6),
            ViolationBaselineLabelLengthRule(255),
            ViolationBaselineCodePrefixRule("Q"),
        ),
    )
}
