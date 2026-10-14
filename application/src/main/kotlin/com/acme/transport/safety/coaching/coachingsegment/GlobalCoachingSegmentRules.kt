package com.acme.transport.safety.coaching.coachingsegment

/** A rule a global coaching segment must pass. Check returns its problems, or none. */
interface GlobalCoachingSegmentRule {
    fun check(item: GlobalCoachingSegment): MutableList<String>
}

data class GlobalCoachingSegmentRequiredLaneRule(var message: String) : GlobalCoachingSegmentRule {
    override fun check(item: GlobalCoachingSegment): MutableList<String> {
        if (item.lane.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalCoachingSegmentBatchSizeRangeRule(var min: Int, var max: Int) : GlobalCoachingSegmentRule {
    override fun check(item: GlobalCoachingSegment): MutableList<String> {
        val value = item.batchSize
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("batch size $value is below $min")
        }
        if (value > max) {
            problems.add("batch size $value is above $max")
        }
        return problems
    }
}

data class GlobalCoachingSegmentRevisionRangeRule(var min: Int, var max: Int) : GlobalCoachingSegmentRule {
    override fun check(item: GlobalCoachingSegment): MutableList<String> {
        val value = item.revision
        if (value < min || value > max) {
            return mutableListOf<String>("revision $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class GlobalCoachingSegmentRequiredCodeRule(var message: String) : GlobalCoachingSegmentRule {
    override fun check(item: GlobalCoachingSegment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global coaching segment. */
data class GlobalCoachingSegmentValidator(var rules: MutableList<GlobalCoachingSegmentRule>) {
    fun validate(item: GlobalCoachingSegment): MutableList<String> {
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

    fun requireValid(item: GlobalCoachingSegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalCoachingSegment>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalCoachingSegmentValidator(): GlobalCoachingSegmentValidator {
    return GlobalCoachingSegmentValidator(
        mutableListOf<GlobalCoachingSegmentRule>(
            GlobalCoachingSegmentRequiredLaneRule("global coaching segment needs an entry"),
            GlobalCoachingSegmentBatchSizeRangeRule(32, 255),
            GlobalCoachingSegmentRevisionRangeRule(32, 80),
            GlobalCoachingSegmentRequiredCodeRule("global coaching segment needs a value"),
        ),
    )
}
