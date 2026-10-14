package com.acme.transport.fleetops.tire.milestone

/** A rule a tire milestone must pass. Check returns its problems, or none. */
interface TireMilestoneRule {
    fun check(item: TireMilestone): MutableList<String>
}

data class TireMilestoneWeightRangeRule(var min: Double, var max: Double) : TireMilestoneRule {
    override fun check(item: TireMilestone): MutableList<String> {
        val value = item.weight
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("weight $value is below $min")
        }
        if (value > max) {
            problems.add("weight $value is above $max")
        }
        return problems
    }
}

data class TireMilestoneThresholdRangeRule(var min: Double, var max: Double) : TireMilestoneRule {
    override fun check(item: TireMilestone): MutableList<String> {
        val value = item.threshold
        if (value < min || value > max) {
            return mutableListOf<String>("threshold $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TireMilestoneRequiredNotesRule(var message: String) : TireMilestoneRule {
    override fun check(item: TireMilestone): MutableList<String> {
        if (item.notes.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TireMilestoneRequiredCodeRule(var message: String) : TireMilestoneRule {
    override fun check(item: TireMilestone): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TireMilestoneTitleLengthRule(var maxLength: Int) : TireMilestoneRule {
    override fun check(item: TireMilestone): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tire milestone. */
data class TireMilestoneValidator(var rules: MutableList<TireMilestoneRule>) {
    fun validate(item: TireMilestone): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TireMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TireMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultTireMilestoneValidator(): TireMilestoneValidator {
    return TireMilestoneValidator(
        mutableListOf<TireMilestoneRule>(
            TireMilestoneWeightRangeRule(0.7, 1912.6),
            TireMilestoneThresholdRangeRule(7.4, 108.5),
            TireMilestoneRequiredNotesRule("tire milestone needs this field"),
            TireMilestoneRequiredCodeRule("tire milestone needs an entry"),
            TireMilestoneTitleLengthRule(255),
        ),
    )
}
