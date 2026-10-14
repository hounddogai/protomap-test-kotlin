package com.acme.transport.crewing.swap.adjustment

/** A rule a swap tier must pass. Check returns its problems, or none. */
interface SwapTierRule {
    fun check(item: SwapTier): MutableList<String>
}

data class SwapTierRequiredDepotRule(var message: String) : SwapTierRule {
    override fun check(item: SwapTier): MutableList<String> {
        if (item.depot.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SwapTierDepotLengthRule(var maxLength: Int) : SwapTierRule {
    override fun check(item: SwapTier): MutableList<String> {
        val size = item.depot.length
        if (size > maxLength) {
            return mutableListOf<String>("depot is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SwapTierRegionLengthRule(var maxLength: Int) : SwapTierRule {
    override fun check(item: SwapTier): MutableList<String> {
        val size = item.region.length
        if (size > maxLength) {
            return mutableListOf<String>("region is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SwapTierCodeLengthRule(var maxLength: Int) : SwapTierRule {
    override fun check(item: SwapTier): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a swap tier. */
data class SwapTierValidator(var rules: MutableList<SwapTierRule>) {
    fun validate(item: SwapTier): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SwapTier) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SwapTier>): MutableMap<String, MutableList<String>> {
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

fun defaultSwapTierValidator(): SwapTierValidator {
    return SwapTierValidator(
        mutableListOf<SwapTierRule>(
            SwapTierRequiredDepotRule("swap tier needs a value"),
            SwapTierDepotLengthRule(3600),
            SwapTierRegionLengthRule(16),
            SwapTierCodeLengthRule(86400),
        ),
    )
}
