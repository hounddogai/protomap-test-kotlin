package com.acme.transport.network.transittime.overrideitem

/** A rule a transit time override must pass. Check returns its problems, or none. */
interface TransitTimeOverrideRule {
    fun check(item: TransitTimeOverride): MutableList<String>
}

data class TransitTimeOverrideRequiredNameRule(var message: String) : TransitTimeOverrideRule {
    override fun check(item: TransitTimeOverride): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TransitTimeOverrideRequiredCodeRule(var message: String) : TransitTimeOverrideRule {
    override fun check(item: TransitTimeOverride): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TransitTimeOverrideFloorValueRangeRule(var min: Double, var max: Double) : TransitTimeOverrideRule {
    override fun check(item: TransitTimeOverride): MutableList<String> {
        val value = item.floorValue
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("floor value $value is below $min")
        }
        if (value > max) {
            problems.add("floor value $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a transit time override. */
data class TransitTimeOverrideValidator(var rules: MutableList<TransitTimeOverrideRule>) {
    fun validate(item: TransitTimeOverride): MutableList<String> {
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

    fun requireValid(item: TransitTimeOverride) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TransitTimeOverride>): MutableMap<String, MutableList<String>> {
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

fun defaultTransitTimeOverrideValidator(): TransitTimeOverrideValidator {
    return TransitTimeOverrideValidator(
        mutableListOf<TransitTimeOverrideRule>(
            TransitTimeOverrideRequiredNameRule("transit time override needs an entry"),
            TransitTimeOverrideRequiredCodeRule("transit time override needs an entry"),
            TransitTimeOverrideFloorValueRangeRule(8.7, 2406.3),
        ),
    )
}
