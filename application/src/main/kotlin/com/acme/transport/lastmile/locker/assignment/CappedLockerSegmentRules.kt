package com.acme.transport.lastmile.locker.assignment

/** A rule a capped locker segment must pass. Check returns its problems, or none. */
interface CappedLockerSegmentRule {
    fun check(item: CappedLockerSegment): MutableList<String>
}

data class CappedLockerSegmentRequiredChannelRule(var message: String) : CappedLockerSegmentRule {
    override fun check(item: CappedLockerSegment): MutableList<String> {
        if (item.channel.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedLockerSegmentRequiredLabelRule(var message: String) : CappedLockerSegmentRule {
    override fun check(item: CappedLockerSegment): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedLockerSegmentCeilingRangeRule(var min: Double, var max: Double) : CappedLockerSegmentRule {
    override fun check(item: CappedLockerSegment): MutableList<String> {
        val value = item.ceiling
        if (value < min || value > max) {
            return mutableListOf<String>("ceiling $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped locker segment. */
data class CappedLockerSegmentValidator(var rules: MutableList<CappedLockerSegmentRule>) {
    fun validate(item: CappedLockerSegment): MutableList<String> {
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

    fun requireValid(item: CappedLockerSegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedLockerSegment>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedLockerSegmentValidator(): CappedLockerSegmentValidator {
    return CappedLockerSegmentValidator(
        mutableListOf<CappedLockerSegmentRule>(
            CappedLockerSegmentRequiredChannelRule("capped locker segment needs this field"),
            CappedLockerSegmentRequiredLabelRule("capped locker segment needs a value"),
            CappedLockerSegmentCeilingRangeRule(4.6, 3963.9),
        ),
    )
}
