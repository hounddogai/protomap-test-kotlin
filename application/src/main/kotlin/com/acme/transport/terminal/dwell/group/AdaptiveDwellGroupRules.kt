package com.acme.transport.terminal.dwell.group

/** A rule a adaptive dwell group must pass. Check returns its problems, or none. */
interface AdaptiveDwellGroupRule {
    fun check(item: AdaptiveDwellGroup): MutableList<String>
}

data class AdaptiveDwellGroupDockLengthRule(var maxLength: Int) : AdaptiveDwellGroupRule {
    override fun check(item: AdaptiveDwellGroup): MutableList<String> {
        val size = item.dock.length
        if (size > maxLength) {
            return mutableListOf<String>("dock is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveDwellGroupGraceDaysRangeRule(var min: Int, var max: Int) : AdaptiveDwellGroupRule {
    override fun check(item: AdaptiveDwellGroup): MutableList<String> {
        val value = item.graceDays
        if (value < min || value > max) {
            return mutableListOf<String>("grace days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveDwellGroupCodePrefixRule(var prefix: String) : AdaptiveDwellGroupRule {
    override fun check(item: AdaptiveDwellGroup): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive dwell group. */
data class AdaptiveDwellGroupValidator(var rules: MutableList<AdaptiveDwellGroupRule>) {
    fun validate(item: AdaptiveDwellGroup): MutableList<String> {
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

    fun requireValid(item: AdaptiveDwellGroup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveDwellGroup>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveDwellGroupValidator(): AdaptiveDwellGroupValidator {
    return AdaptiveDwellGroupValidator(
        mutableListOf<AdaptiveDwellGroupRule>(
            AdaptiveDwellGroupDockLengthRule(16),
            AdaptiveDwellGroupGraceDaysRangeRule(32, 3600),
            AdaptiveDwellGroupCodePrefixRule("OPS"),
        ),
    )
}
