package com.acme.transport.safety.nearmiss.policy

/** A rule a near miss segment must pass. Check returns its problems, or none. */
interface NearMissSegmentRule {
    fun check(item: NearMissSegment): MutableList<String>
}

data class NearMissSegmentRequiredCodeRule(var message: String) : NearMissSegmentRule {
    override fun check(item: NearMissSegment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class NearMissSegmentEndsAtRangeRule(var min: Int, var max: Int) : NearMissSegmentRule {
    override fun check(item: NearMissSegment): MutableList<String> {
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

data class NearMissSegmentRequiredDriverIdRule(var message: String) : NearMissSegmentRule {
    override fun check(item: NearMissSegment): MutableList<String> {
        if (item.driverId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class NearMissSegmentCodePrefixRule(var prefix: String) : NearMissSegmentRule {
    override fun check(item: NearMissSegment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class NearMissSegmentRequiredRouteCodeRule(var message: String) : NearMissSegmentRule {
    override fun check(item: NearMissSegment): MutableList<String> {
        if (item.routeCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a near miss segment. */
data class NearMissSegmentValidator(var rules: MutableList<NearMissSegmentRule>) {
    fun validate(item: NearMissSegment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: NearMissSegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<NearMissSegment>): MutableMap<String, MutableList<String>> {
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

fun defaultNearMissSegmentValidator(): NearMissSegmentValidator {
    return NearMissSegmentValidator(
        mutableListOf<NearMissSegmentRule>(
            NearMissSegmentRequiredCodeRule("near miss segment needs a value"),
            NearMissSegmentEndsAtRangeRule(16, 80),
            NearMissSegmentRequiredDriverIdRule("near miss segment needs an entry"),
            NearMissSegmentCodePrefixRule("OPS"),
            NearMissSegmentRequiredRouteCodeRule("near miss segment needs an entry"),
        ),
    )
}
