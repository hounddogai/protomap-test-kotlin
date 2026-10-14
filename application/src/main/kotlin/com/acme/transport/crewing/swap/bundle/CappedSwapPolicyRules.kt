package com.acme.transport.crewing.swap.bundle

/** A rule a capped swap policy must pass. Check returns its problems, or none. */
interface CappedSwapPolicyRule {
    fun check(item: CappedSwapPolicy): MutableList<String>
}

data class CappedSwapPolicyRequiredTimezoneRule(var message: String) : CappedSwapPolicyRule {
    override fun check(item: CappedSwapPolicy): MutableList<String> {
        if (item.timezone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedSwapPolicyLoadKgRangeRule(var min: Double, var max: Double) : CappedSwapPolicyRule {
    override fun check(item: CappedSwapPolicy): MutableList<String> {
        val value = item.loadKg
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("load kg $value is below $min")
        }
        if (value > max) {
            problems.add("load kg $value is above $max")
        }
        return problems
    }
}

data class CappedSwapPolicyCodePrefixRule(var prefix: String) : CappedSwapPolicyRule {
    override fun check(item: CappedSwapPolicy): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class CappedSwapPolicyFloorValueRangeRule(var min: Double, var max: Double) : CappedSwapPolicyRule {
    override fun check(item: CappedSwapPolicy): MutableList<String> {
        val value = item.floorValue
        if (value < min || value > max) {
            return mutableListOf<String>("floor value $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped swap policy. */
data class CappedSwapPolicyValidator(var rules: MutableList<CappedSwapPolicyRule>) {
    fun validate(item: CappedSwapPolicy): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CappedSwapPolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedSwapPolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedSwapPolicyValidator(): CappedSwapPolicyValidator {
    return CappedSwapPolicyValidator(
        mutableListOf<CappedSwapPolicyRule>(
            CappedSwapPolicyRequiredTimezoneRule("capped swap policy needs a value"),
            CappedSwapPolicyLoadKgRangeRule(4.2, 4976.9),
            CappedSwapPolicyCodePrefixRule("X"),
            CappedSwapPolicyFloorValueRangeRule(0.3, 1964.5),
        ),
    )
}
