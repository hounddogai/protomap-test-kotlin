package com.acme.transport.crewing.roster.exception

/** A rule a roster estimate must pass. Check returns its problems, or none. */
interface RosterEstimateRule {
    fun check(item: RosterEstimate): MutableList<String>
}

data class RosterEstimatePriorityRangeRule(var min: Int, var max: Int) : RosterEstimateRule {
    override fun check(item: RosterEstimate): MutableList<String> {
        val value = item.priority
        if (value < min || value > max) {
            return mutableListOf<String>("priority $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RosterEstimateCodeLengthRule(var maxLength: Int) : RosterEstimateRule {
    override fun check(item: RosterEstimate): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RosterEstimateTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : RosterEstimateRule {
    override fun check(item: RosterEstimate): MutableList<String> {
        val problems = mutableListOf<String>()
        if (item.tags.size > maxCount) {
            problems.add("has ${item.tags.size} tags, at most $maxCount allowed")
        }
        for (tag in item.tags) {
            if (banned.contains(tag)) {
                problems.add("tag $tag is not allowed")
            }
        }
        return problems
    }
}

data class RosterEstimateScoreRangeRule(var min: Double, var max: Double) : RosterEstimateRule {
    override fun check(item: RosterEstimate): MutableList<String> {
        val value = item.score
        if (value < min || value > max) {
            return mutableListOf<String>("score $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RosterEstimateRequiredContactEmailRule(var message: String) : RosterEstimateRule {
    override fun check(item: RosterEstimate): MutableList<String> {
        if (item.contactEmail.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RosterEstimateRequiredCodeRule(var message: String) : RosterEstimateRule {
    override fun check(item: RosterEstimate): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a roster estimate. */
data class RosterEstimateValidator(var rules: MutableList<RosterEstimateRule>) {
    fun validate(item: RosterEstimate): MutableList<String> {
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

    fun requireValid(item: RosterEstimate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RosterEstimate>): MutableMap<String, MutableList<String>> {
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

fun defaultRosterEstimateValidator(): RosterEstimateValidator {
    return RosterEstimateValidator(
        mutableListOf<RosterEstimateRule>(
            RosterEstimatePriorityRangeRule(80, 255),
            RosterEstimateCodeLengthRule(64),
            RosterEstimateTagsLimitRule(120, mutableListOf<String>("global", "global")),
            RosterEstimateScoreRangeRule(3.9, 4277.7),
            RosterEstimateRequiredContactEmailRule("roster estimate needs this field"),
            RosterEstimateRequiredCodeRule("roster estimate needs this field"),
        ),
    )
}
