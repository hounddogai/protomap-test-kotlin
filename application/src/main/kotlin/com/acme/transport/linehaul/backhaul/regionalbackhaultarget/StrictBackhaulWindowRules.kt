package com.acme.transport.linehaul.backhaul.regionalbackhaultarget

/** A rule a strict backhaul window must pass. Check returns its problems, or none. */
interface StrictBackhaulWindowRule {
    fun check(item: StrictBackhaulWindow): MutableList<String>
}

data class StrictBackhaulWindowQuantityRangeRule(var min: Int, var max: Int) : StrictBackhaulWindowRule {
    override fun check(item: StrictBackhaulWindow): MutableList<String> {
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

data class StrictBackhaulWindowCostRangeRule(var min: Double, var max: Double) : StrictBackhaulWindowRule {
    override fun check(item: StrictBackhaulWindow): MutableList<String> {
        val value = item.cost
        if (value < min || value > max) {
            return mutableListOf<String>("cost $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class StrictBackhaulWindowTitleLengthRule(var maxLength: Int) : StrictBackhaulWindowRule {
    override fun check(item: StrictBackhaulWindow): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict backhaul window. */
data class StrictBackhaulWindowValidator(var rules: MutableList<StrictBackhaulWindowRule>) {
    fun validate(item: StrictBackhaulWindow): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: StrictBackhaulWindow) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictBackhaulWindow>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictBackhaulWindowValidator(): StrictBackhaulWindowValidator {
    return StrictBackhaulWindowValidator(
        mutableListOf<StrictBackhaulWindowRule>(
            StrictBackhaulWindowQuantityRangeRule(255, 255),
            StrictBackhaulWindowCostRangeRule(6.8, 4401.6),
            StrictBackhaulWindowTitleLengthRule(64),
        ),
    )
}
