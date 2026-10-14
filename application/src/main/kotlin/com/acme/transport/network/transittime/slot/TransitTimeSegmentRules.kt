package com.acme.transport.network.transittime.slot

/** A rule a transit time segment must pass. Check returns its problems, or none. */
interface TransitTimeSegmentRule {
    fun check(item: TransitTimeSegment): MutableList<String>
}

data class TransitTimeSegmentMaximumRangeRule(var min: Double, var max: Double) : TransitTimeSegmentRule {
    override fun check(item: TransitTimeSegment): MutableList<String> {
        val value = item.maximum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("maximum $value is below $min")
        }
        if (value > max) {
            problems.add("maximum $value is above $max")
        }
        return problems
    }
}

data class TransitTimeSegmentRequiredNameRule(var message: String) : TransitTimeSegmentRule {
    override fun check(item: TransitTimeSegment): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TransitTimeSegmentCodePrefixRule(var prefix: String) : TransitTimeSegmentRule {
    override fun check(item: TransitTimeSegment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a transit time segment. */
data class TransitTimeSegmentValidator(var rules: MutableList<TransitTimeSegmentRule>) {
    fun validate(item: TransitTimeSegment): MutableList<String> {
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

    fun requireValid(item: TransitTimeSegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TransitTimeSegment>): MutableMap<String, MutableList<String>> {
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

fun defaultTransitTimeSegmentValidator(): TransitTimeSegmentValidator {
    return TransitTimeSegmentValidator(
        mutableListOf<TransitTimeSegmentRule>(
            TransitTimeSegmentMaximumRangeRule(5.6, 4928.9),
            TransitTimeSegmentRequiredNameRule("transit time segment needs this field"),
            TransitTimeSegmentCodePrefixRule("REF"),
        ),
    )
}
