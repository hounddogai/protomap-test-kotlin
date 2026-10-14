package com.acme.transport.lastmile.drop.rule

/** A rule a strict drop group must pass. Check returns its problems, or none. */
interface StrictDropGroupRule {
    fun check(item: StrictDropGroup): MutableList<String>
}

data class StrictDropGroupQuantityRangeRule(var min: Int, var max: Int) : StrictDropGroupRule {
    override fun check(item: StrictDropGroup): MutableList<String> {
        val value = item.quantity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("quantity $value is below $min")
        }
        if (value > max) {
            problems.add("quantity $value is above $max")
        }
        return problems
    }
}

data class StrictDropGroupCodePrefixRule(var prefix: String) : StrictDropGroupRule {
    override fun check(item: StrictDropGroup): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class StrictDropGroupTitleLengthRule(var maxLength: Int) : StrictDropGroupRule {
    override fun check(item: StrictDropGroup): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict drop group. */
data class StrictDropGroupValidator(var rules: MutableList<StrictDropGroupRule>) {
    fun validate(item: StrictDropGroup): MutableList<String> {
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

    fun requireValid(item: StrictDropGroup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictDropGroup>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictDropGroupValidator(): StrictDropGroupValidator {
    return StrictDropGroupValidator(
        mutableListOf<StrictDropGroupRule>(
            StrictDropGroupQuantityRangeRule(32, 120),
            StrictDropGroupCodePrefixRule("REF"),
            StrictDropGroupTitleLengthRule(3600),
        ),
    )
}
