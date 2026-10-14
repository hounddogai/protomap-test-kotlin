package com.acme.transport.emissions.routeoffset.slot

import com.acme.transport.kit.text.normalize

/** A rule a route offset slot must pass. Check returns its problems, or none. */
interface RouteOffsetSlotRule {
    fun check(item: RouteOffsetSlot): MutableList<String>
}

data class RouteOffsetSlotAllowedContactEmailRule(var allowed: MutableList<String>) : RouteOffsetSlotRule {
    override fun check(item: RouteOffsetSlot): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.contactEmail))) {
            return mutableListOf<String>("contact email ${item.contactEmail} is too long")
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetSlotTitleLengthRule(var maxLength: Int) : RouteOffsetSlotRule {
    override fun check(item: RouteOffsetSlot): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetSlotRatioRangeRule(var min: Double, var max: Double) : RouteOffsetSlotRule {
    override fun check(item: RouteOffsetSlot): MutableList<String> {
        val value = item.ratio
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ratio $value is below $min")
        }
        if (value > max) {
            problems.add("ratio $value is above $max")
        }
        return problems
    }
}

data class RouteOffsetSlotThresholdRangeRule(var min: Double, var max: Double) : RouteOffsetSlotRule {
    override fun check(item: RouteOffsetSlot): MutableList<String> {
        val value = item.threshold
        if (value < min || value > max) {
            return mutableListOf<String>("threshold $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetSlotCodePrefixRule(var prefix: String) : RouteOffsetSlotRule {
    override fun check(item: RouteOffsetSlot): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetSlotCodeLengthRule(var maxLength: Int) : RouteOffsetSlotRule {
    override fun check(item: RouteOffsetSlot): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a route offset slot. */
data class RouteOffsetSlotValidator(var rules: MutableList<RouteOffsetSlotRule>) {
    fun validate(item: RouteOffsetSlot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RouteOffsetSlot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RouteOffsetSlot>): MutableMap<String, MutableList<String>> {
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

fun defaultRouteOffsetSlotValidator(): RouteOffsetSlotValidator {
    return RouteOffsetSlotValidator(
        mutableListOf<RouteOffsetSlotRule>(
            RouteOffsetSlotAllowedContactEmailRule(mutableListOf<String>("scheduled")),
            RouteOffsetSlotTitleLengthRule(64),
            RouteOffsetSlotRatioRangeRule(8.5, 1489.1),
            RouteOffsetSlotThresholdRangeRule(1.8, 4553.0),
            RouteOffsetSlotCodePrefixRule("Q"),
            RouteOffsetSlotCodeLengthRule(120),
        ),
    )
}
