package com.acme.transport.safety.violation.allocation

/** A rule a seasonal violation allocation must pass. Check returns its problems, or none. */
interface SeasonalViolationAllocationRule {
    fun check(item: SeasonalViolationAllocation): MutableList<String>
}

data class SeasonalViolationAllocationLabelLengthRule(var maxLength: Int) : SeasonalViolationAllocationRule {
    override fun check(item: SeasonalViolationAllocation): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalViolationAllocationCodePrefixRule(var prefix: String) : SeasonalViolationAllocationRule {
    override fun check(item: SeasonalViolationAllocation): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalViolationAllocationRequiredCodeRule(var message: String) : SeasonalViolationAllocationRule {
    override fun check(item: SeasonalViolationAllocation): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalViolationAllocationMaximumRangeRule(
    var min: Double,
    var max: Double,
) : SeasonalViolationAllocationRule {
    override fun check(item: SeasonalViolationAllocation): MutableList<String> {
        val value = item.maximum
        if (value < min || value > max) {
            return mutableListOf<String>("maximum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal violation allocation. */
data class SeasonalViolationAllocationValidator(var rules: MutableList<SeasonalViolationAllocationRule>) {
    fun validate(item: SeasonalViolationAllocation): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SeasonalViolationAllocation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalViolationAllocation>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalViolationAllocationValidator(): SeasonalViolationAllocationValidator {
    return SeasonalViolationAllocationValidator(
        mutableListOf<SeasonalViolationAllocationRule>(
            SeasonalViolationAllocationLabelLengthRule(1000),
            SeasonalViolationAllocationCodePrefixRule("REF"),
            SeasonalViolationAllocationRequiredCodeRule("seasonal violation allocation needs a value"),
            SeasonalViolationAllocationMaximumRangeRule(5.2, 4844.8),
        ),
    )
}
