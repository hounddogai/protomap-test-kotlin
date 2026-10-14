package com.acme.transport.safety.nearmiss.missvariance

/** A rule a scheduled near miss segment must pass. Check returns its problems, or none. */
interface ScheduledNearMissSegmentRule {
    fun check(item: ScheduledNearMissSegment): MutableList<String>
}

data class ScheduledNearMissSegmentRequiredRouteCodeRule(var message: String) : ScheduledNearMissSegmentRule {
    override fun check(item: ScheduledNearMissSegment): MutableList<String> {
        if (item.routeCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledNearMissSegmentCodeLengthRule(var maxLength: Int) : ScheduledNearMissSegmentRule {
    override fun check(item: ScheduledNearMissSegment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledNearMissSegmentVersionRangeRule(var min: Int, var max: Int) : ScheduledNearMissSegmentRule {
    override fun check(item: ScheduledNearMissSegment): MutableList<String> {
        val value = item.version
        if (value < min || value > max) {
            return mutableListOf<String>("version $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledNearMissSegmentPhoneLengthRule(var maxLength: Int) : ScheduledNearMissSegmentRule {
    override fun check(item: ScheduledNearMissSegment): MutableList<String> {
        val size = item.phone.length
        if (size > maxLength) {
            return mutableListOf<String>("phone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledNearMissSegmentNameLengthRule(var maxLength: Int) : ScheduledNearMissSegmentRule {
    override fun check(item: ScheduledNearMissSegment): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledNearMissSegmentRequiredNameRule(var message: String) : ScheduledNearMissSegmentRule {
    override fun check(item: ScheduledNearMissSegment): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled near miss segment. */
data class ScheduledNearMissSegmentValidator(var rules: MutableList<ScheduledNearMissSegmentRule>) {
    fun validate(item: ScheduledNearMissSegment): MutableList<String> {
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

    fun requireValid(item: ScheduledNearMissSegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledNearMissSegment>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledNearMissSegmentValidator(): ScheduledNearMissSegmentValidator {
    return ScheduledNearMissSegmentValidator(
        mutableListOf<ScheduledNearMissSegmentRule>(
            ScheduledNearMissSegmentRequiredRouteCodeRule("scheduled near miss segment needs an entry"),
            ScheduledNearMissSegmentCodeLengthRule(64),
            ScheduledNearMissSegmentVersionRangeRule(64, 64),
            ScheduledNearMissSegmentPhoneLengthRule(64),
            ScheduledNearMissSegmentNameLengthRule(32),
            ScheduledNearMissSegmentRequiredNameRule("scheduled near miss segment needs a value"),
        ),
    )
}
