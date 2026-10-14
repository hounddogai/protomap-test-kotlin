package com.acme.transport.emissions.routeoffset.offsetbaseline

/** A rule a strict route offset target must pass. Check returns its problems, or none. */
interface StrictRouteOffsetTargetRule {
    fun check(item: StrictRouteOffsetTarget): MutableList<String>
}

data class StrictRouteOffsetTargetStartsAtRangeRule(var min: Int, var max: Int) : StrictRouteOffsetTargetRule {
    override fun check(item: StrictRouteOffsetTarget): MutableList<String> {
        val value = item.startsAt
        if (value < min || value > max) {
            return mutableListOf<String>("starts at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class StrictRouteOffsetTargetCodePrefixRule(var prefix: String) : StrictRouteOffsetTargetRule {
    override fun check(item: StrictRouteOffsetTarget): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class StrictRouteOffsetTargetDepotLengthRule(var maxLength: Int) : StrictRouteOffsetTargetRule {
    override fun check(item: StrictRouteOffsetTarget): MutableList<String> {
        val size = item.depot.length
        if (size > maxLength) {
            return mutableListOf<String>("depot is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictRouteOffsetTargetLabelLengthRule(var maxLength: Int) : StrictRouteOffsetTargetRule {
    override fun check(item: StrictRouteOffsetTarget): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict route offset target. */
data class StrictRouteOffsetTargetValidator(var rules: MutableList<StrictRouteOffsetTargetRule>) {
    fun validate(item: StrictRouteOffsetTarget): MutableList<String> {
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

    fun requireValid(item: StrictRouteOffsetTarget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictRouteOffsetTarget>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictRouteOffsetTargetValidator(): StrictRouteOffsetTargetValidator {
    return StrictRouteOffsetTargetValidator(
        mutableListOf<StrictRouteOffsetTargetRule>(
            StrictRouteOffsetTargetStartsAtRangeRule(255, 3600),
            StrictRouteOffsetTargetCodePrefixRule("OPS"),
            StrictRouteOffsetTargetDepotLengthRule(32),
            StrictRouteOffsetTargetLabelLengthRule(255),
        ),
    )
}
