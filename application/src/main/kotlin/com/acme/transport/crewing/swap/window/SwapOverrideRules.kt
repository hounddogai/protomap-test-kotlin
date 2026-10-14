package com.acme.transport.crewing.swap.window

/** A rule a swap override must pass. Check returns its problems, or none. */
interface SwapOverrideRule {
    fun check(item: SwapOverride): MutableList<String>
}

data class SwapOverrideRatioRangeRule(var min: Double, var max: Double) : SwapOverrideRule {
    override fun check(item: SwapOverride): MutableList<String> {
        val value = item.ratio
        if (value < min || value > max) {
            return mutableListOf<String>("ratio $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class SwapOverrideEtaMinutesRangeRule(var min: Int, var max: Int) : SwapOverrideRule {
    override fun check(item: SwapOverride): MutableList<String> {
        val value = item.etaMinutes
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("eta minutes $value is below $min")
        }
        if (value > max) {
            problems.add("eta minutes $value is above $max")
        }
        return problems
    }
}

data class SwapOverrideRequiredNameRule(var message: String) : SwapOverrideRule {
    override fun check(item: SwapOverride): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SwapOverrideRequiredDepotRule(var message: String) : SwapOverrideRule {
    override fun check(item: SwapOverride): MutableList<String> {
        if (item.depot.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a swap override. */
data class SwapOverrideValidator(var rules: MutableList<SwapOverrideRule>) {
    fun validate(item: SwapOverride): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SwapOverride) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SwapOverride>): MutableMap<String, MutableList<String>> {
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

fun defaultSwapOverrideValidator(): SwapOverrideValidator {
    return SwapOverrideValidator(
        mutableListOf<SwapOverrideRule>(
            SwapOverrideRatioRangeRule(6.2, 107.0),
            SwapOverrideEtaMinutesRangeRule(32, 86400),
            SwapOverrideRequiredNameRule("swap override needs a value"),
            SwapOverrideRequiredDepotRule("swap override needs a value"),
        ),
    )
}
