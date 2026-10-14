package com.acme.transport.emissions.routeoffset.offsetnotice

/** A rule a strict route offset segment must pass. Check returns its problems, or none. */
interface StrictRouteOffsetSegmentRule {
    fun check(item: StrictRouteOffsetSegment): MutableList<String>
}

data class StrictRouteOffsetSegmentCodeLengthRule(var maxLength: Int) : StrictRouteOffsetSegmentRule {
    override fun check(item: StrictRouteOffsetSegment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictRouteOffsetSegmentLaneLengthRule(var maxLength: Int) : StrictRouteOffsetSegmentRule {
    override fun check(item: StrictRouteOffsetSegment): MutableList<String> {
        val size = item.lane.length
        if (size > maxLength) {
            return mutableListOf<String>("lane is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictRouteOffsetSegmentRequiredLabelRule(var message: String) : StrictRouteOffsetSegmentRule {
    override fun check(item: StrictRouteOffsetSegment): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictRouteOffsetSegmentCodePrefixRule(var prefix: String) : StrictRouteOffsetSegmentRule {
    override fun check(item: StrictRouteOffsetSegment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict route offset segment. */
data class StrictRouteOffsetSegmentValidator(var rules: MutableList<StrictRouteOffsetSegmentRule>) {
    fun validate(item: StrictRouteOffsetSegment): MutableList<String> {
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

    fun requireValid(item: StrictRouteOffsetSegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictRouteOffsetSegment>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictRouteOffsetSegmentValidator(): StrictRouteOffsetSegmentValidator {
    return StrictRouteOffsetSegmentValidator(
        mutableListOf<StrictRouteOffsetSegmentRule>(
            StrictRouteOffsetSegmentCodeLengthRule(64),
            StrictRouteOffsetSegmentLaneLengthRule(1000),
            StrictRouteOffsetSegmentRequiredLabelRule("strict route offset segment needs a value"),
            StrictRouteOffsetSegmentCodePrefixRule("REF"),
        ),
    )
}
