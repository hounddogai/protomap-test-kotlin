package com.acme.transport.network.servicearea.exception

/** A rule a lenient service area request must pass. Check returns its problems, or none. */
interface LenientServiceAreaRequestRule {
    fun check(item: LenientServiceAreaRequest): MutableList<String>
}

data class LenientServiceAreaRequestCodePrefixRule(var prefix: String) : LenientServiceAreaRequestRule {
    override fun check(item: LenientServiceAreaRequest): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LenientServiceAreaRequestUpdatedAtRangeRule(
    var min: Int,
    var max: Int,
) : LenientServiceAreaRequestRule {
    override fun check(item: LenientServiceAreaRequest): MutableList<String> {
        val value = item.updatedAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("updated at $value is below $min")
        }
        if (value > max) {
            problems.add("updated at $value is above $max")
        }
        return problems
    }
}

data class LenientServiceAreaRequestRequiredTimezoneRule(var message: String) : LenientServiceAreaRequestRule {
    override fun check(item: LenientServiceAreaRequest): MutableList<String> {
        if (item.timezone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient service area request. */
data class LenientServiceAreaRequestValidator(var rules: MutableList<LenientServiceAreaRequestRule>) {
    fun validate(item: LenientServiceAreaRequest): MutableList<String> {
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

    fun requireValid(item: LenientServiceAreaRequest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientServiceAreaRequest>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientServiceAreaRequestValidator(): LenientServiceAreaRequestValidator {
    return LenientServiceAreaRequestValidator(
        mutableListOf<LenientServiceAreaRequestRule>(
            LenientServiceAreaRequestCodePrefixRule("OPS"),
            LenientServiceAreaRequestUpdatedAtRangeRule(64, 64),
            LenientServiceAreaRequestRequiredTimezoneRule("lenient service area request needs a value"),
        ),
    )
}
