package com.acme.transport.network.servicearea.agreement

/** A rule a lenient service area rollup must pass. Check returns its problems, or none. */
interface LenientServiceAreaRollupRule {
    fun check(item: LenientServiceAreaRollup): MutableList<String>
}

data class LenientServiceAreaRollupCodeLengthRule(var maxLength: Int) : LenientServiceAreaRollupRule {
    override fun check(item: LenientServiceAreaRollup): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientServiceAreaRollupRequiredCodeRule(var message: String) : LenientServiceAreaRollupRule {
    override fun check(item: LenientServiceAreaRollup): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientServiceAreaRollupCostRangeRule(
    var min: Double,
    var max: Double,
) : LenientServiceAreaRollupRule {
    override fun check(item: LenientServiceAreaRollup): MutableList<String> {
        val value = item.cost
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("cost $value is below $min")
        }
        if (value > max) {
            problems.add("cost $value is above $max")
        }
        return problems
    }
}

data class LenientServiceAreaRollupLoadKgRangeRule(
    var min: Double,
    var max: Double,
) : LenientServiceAreaRollupRule {
    override fun check(item: LenientServiceAreaRollup): MutableList<String> {
        val value = item.loadKg
        if (value < min || value > max) {
            return mutableListOf<String>("load kg $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LenientServiceAreaRollupCodePrefixRule(var prefix: String) : LenientServiceAreaRollupRule {
    override fun check(item: LenientServiceAreaRollup): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LenientServiceAreaRollupStopCountRangeRule(
    var min: Int,
    var max: Int,
) : LenientServiceAreaRollupRule {
    override fun check(item: LenientServiceAreaRollup): MutableList<String> {
        val value = item.stopCount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("stop count $value is below $min")
        }
        if (value > max) {
            problems.add("stop count $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a lenient service area rollup. */
data class LenientServiceAreaRollupValidator(var rules: MutableList<LenientServiceAreaRollupRule>) {
    fun validate(item: LenientServiceAreaRollup): MutableList<String> {
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

    fun requireValid(item: LenientServiceAreaRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientServiceAreaRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientServiceAreaRollupValidator(): LenientServiceAreaRollupValidator {
    return LenientServiceAreaRollupValidator(
        mutableListOf<LenientServiceAreaRollupRule>(
            LenientServiceAreaRollupCodeLengthRule(120),
            LenientServiceAreaRollupRequiredCodeRule("lenient service area rollup needs a value"),
            LenientServiceAreaRollupCostRangeRule(7.2, 1151.7),
            LenientServiceAreaRollupLoadKgRangeRule(5.3, 4490.8),
            LenientServiceAreaRollupCodePrefixRule("ACM"),
            LenientServiceAreaRollupStopCountRangeRule(16, 80),
        ),
    )
}
