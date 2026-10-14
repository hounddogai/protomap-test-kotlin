package com.acme.transport.network.hub.estimate

/** A rule a lenient hub baseline must pass. Check returns its problems, or none. */
interface LenientHubBaselineRule {
    fun check(item: LenientHubBaseline): MutableList<String>
}

data class LenientHubBaselineCodePrefixRule(var prefix: String) : LenientHubBaselineRule {
    override fun check(item: LenientHubBaseline): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LenientHubBaselineEndsAtRangeRule(var min: Int, var max: Int) : LenientHubBaselineRule {
    override fun check(item: LenientHubBaseline): MutableList<String> {
        val value = item.endsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ends at $value is below $min")
        }
        if (value > max) {
            problems.add("ends at $value is above $max")
        }
        return problems
    }
}

data class LenientHubBaselineLoadKgRangeRule(var min: Double, var max: Double) : LenientHubBaselineRule {
    override fun check(item: LenientHubBaseline): MutableList<String> {
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

data class LenientHubBaselineRequiredCodeRule(var message: String) : LenientHubBaselineRule {
    override fun check(item: LenientHubBaseline): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient hub baseline. */
data class LenientHubBaselineValidator(var rules: MutableList<LenientHubBaselineRule>) {
    fun validate(item: LenientHubBaseline): MutableList<String> {
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

    fun requireValid(item: LenientHubBaseline) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientHubBaseline>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientHubBaselineValidator(): LenientHubBaselineValidator {
    return LenientHubBaselineValidator(
        mutableListOf<LenientHubBaselineRule>(
            LenientHubBaselineCodePrefixRule("OPS"),
            LenientHubBaselineEndsAtRangeRule(120, 1000),
            LenientHubBaselineLoadKgRangeRule(9.9, 338.5),
            LenientHubBaselineRequiredCodeRule("lenient hub baseline needs this field"),
        ),
    )
}
