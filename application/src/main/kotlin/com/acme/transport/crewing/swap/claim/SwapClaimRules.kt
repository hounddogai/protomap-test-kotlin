package com.acme.transport.crewing.swap.claim

/** A rule a swap claim must pass. Check returns its problems, or none. */
interface SwapClaimRule {
    fun check(item: SwapClaim): MutableList<String>
}

data class SwapClaimRequiredSegmentRule(var message: String) : SwapClaimRule {
    override fun check(item: SwapClaim): MutableList<String> {
        if (item.segment.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SwapClaimTitleLengthRule(var maxLength: Int) : SwapClaimRule {
    override fun check(item: SwapClaim): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SwapClaimQuantityRangeRule(var min: Int, var max: Int) : SwapClaimRule {
    override fun check(item: SwapClaim): MutableList<String> {
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

data class SwapClaimCodePrefixRule(var prefix: String) : SwapClaimRule {
    override fun check(item: SwapClaim): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class SwapClaimDistanceKmRangeRule(var min: Double, var max: Double) : SwapClaimRule {
    override fun check(item: SwapClaim): MutableList<String> {
        val value = item.distanceKm
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("distance km $value is below $min")
        }
        if (value > max) {
            problems.add("distance km $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a swap claim. */
data class SwapClaimValidator(var rules: MutableList<SwapClaimRule>) {
    fun validate(item: SwapClaim): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SwapClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SwapClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultSwapClaimValidator(): SwapClaimValidator {
    return SwapClaimValidator(
        mutableListOf<SwapClaimRule>(
            SwapClaimRequiredSegmentRule("swap claim needs a value"),
            SwapClaimTitleLengthRule(1000),
            SwapClaimQuantityRangeRule(16, 255),
            SwapClaimCodePrefixRule("Q"),
            SwapClaimDistanceKmRangeRule(4.8, 4700.3),
        ),
    )
}
