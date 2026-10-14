package com.acme.transport.crewing.restbreak.overrideitem

/** A rule a weighted rest break override must pass. Check returns its problems, or none. */
interface WeightedRestBreakOverrideRule {
    fun check(item: WeightedRestBreakOverride): MutableList<String>
}

data class WeightedRestBreakOverridePalletsRangeRule(
    var min: Int,
    var max: Int,
) : WeightedRestBreakOverrideRule {
    override fun check(item: WeightedRestBreakOverride): MutableList<String> {
        val value = item.pallets
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("pallets $value is below $min")
        }
        if (value > max) {
            problems.add("pallets $value is above $max")
        }
        return problems
    }
}

data class WeightedRestBreakOverrideRequiredLabelRule(var message: String) : WeightedRestBreakOverrideRule {
    override fun check(item: WeightedRestBreakOverride): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedRestBreakOverrideCodePrefixRule(var prefix: String) : WeightedRestBreakOverrideRule {
    override fun check(item: WeightedRestBreakOverride): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted rest break override. */
data class WeightedRestBreakOverrideValidator(var rules: MutableList<WeightedRestBreakOverrideRule>) {
    fun validate(item: WeightedRestBreakOverride): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: WeightedRestBreakOverride) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedRestBreakOverride>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedRestBreakOverrideValidator(): WeightedRestBreakOverrideValidator {
    return WeightedRestBreakOverrideValidator(
        mutableListOf<WeightedRestBreakOverrideRule>(
            WeightedRestBreakOverridePalletsRangeRule(120, 86400),
            WeightedRestBreakOverrideRequiredLabelRule("weighted rest break override needs an entry"),
            WeightedRestBreakOverrideCodePrefixRule("Q"),
        ),
    )
}
