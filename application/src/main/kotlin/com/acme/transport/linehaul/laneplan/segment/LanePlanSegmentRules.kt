package com.acme.transport.linehaul.laneplan.segment

/** A rule a lane plan segment must pass. Check returns its problems, or none. */
interface LanePlanSegmentRule {
    fun check(item: LanePlanSegment): MutableList<String>
}

data class LanePlanSegmentCodePrefixRule(var prefix: String) : LanePlanSegmentRule {
    override fun check(item: LanePlanSegment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LanePlanSegmentNotesLengthRule(var maxLength: Int) : LanePlanSegmentRule {
    override fun check(item: LanePlanSegment): MutableList<String> {
        val size = item.notes.length
        if (size > maxLength) {
            return mutableListOf<String>("notes is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LanePlanSegmentRequiredCodeRule(var message: String) : LanePlanSegmentRule {
    override fun check(item: LanePlanSegment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LanePlanSegmentLimitRangeRule(var min: Int, var max: Int) : LanePlanSegmentRule {
    override fun check(item: LanePlanSegment): MutableList<String> {
        val value = item.limit
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("limit $value is below $min")
        }
        if (value > max) {
            problems.add("limit $value is above $max")
        }
        return problems
    }
}

data class LanePlanSegmentUpdatedAtRangeRule(var min: Int, var max: Int) : LanePlanSegmentRule {
    override fun check(item: LanePlanSegment): MutableList<String> {
        val value = item.updatedAt
        if (value < min || value > max) {
            return mutableListOf<String>("updated at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LanePlanSegmentRequiredTitleRule(var message: String) : LanePlanSegmentRule {
    override fun check(item: LanePlanSegment): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lane plan segment. */
data class LanePlanSegmentValidator(var rules: MutableList<LanePlanSegmentRule>) {
    fun validate(item: LanePlanSegment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LanePlanSegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LanePlanSegment>): MutableMap<String, MutableList<String>> {
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

fun defaultLanePlanSegmentValidator(): LanePlanSegmentValidator {
    return LanePlanSegmentValidator(
        mutableListOf<LanePlanSegmentRule>(
            LanePlanSegmentCodePrefixRule("REF"),
            LanePlanSegmentNotesLengthRule(3600),
            LanePlanSegmentRequiredCodeRule("lane plan segment needs a value"),
            LanePlanSegmentLimitRangeRule(16, 86400),
            LanePlanSegmentUpdatedAtRangeRule(255, 3600),
            LanePlanSegmentRequiredTitleRule("lane plan segment needs a value"),
        ),
    )
}
