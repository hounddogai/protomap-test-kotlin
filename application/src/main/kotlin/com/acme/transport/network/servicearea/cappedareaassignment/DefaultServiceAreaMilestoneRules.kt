package com.acme.transport.network.servicearea.cappedareaassignment

/** A rule a default service area milestone must pass. Check returns its problems, or none. */
interface DefaultServiceAreaMilestoneRule {
    fun check(item: DefaultServiceAreaMilestone): MutableList<String>
}

data class DefaultServiceAreaMilestoneSequenceRangeRule(
    var min: Int,
    var max: Int,
) : DefaultServiceAreaMilestoneRule {
    override fun check(item: DefaultServiceAreaMilestone): MutableList<String> {
        val value = item.sequence
        if (value < min || value > max) {
            return mutableListOf<String>("sequence $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DefaultServiceAreaMilestoneTitleLengthRule(var maxLength: Int) : DefaultServiceAreaMilestoneRule {
    override fun check(item: DefaultServiceAreaMilestone): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultServiceAreaMilestoneGraceDaysRangeRule(
    var min: Int,
    var max: Int,
) : DefaultServiceAreaMilestoneRule {
    override fun check(item: DefaultServiceAreaMilestone): MutableList<String> {
        val value = item.graceDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("grace days $value is below $min")
        }
        if (value > max) {
            problems.add("grace days $value is above $max")
        }
        return problems
    }
}

data class DefaultServiceAreaMilestoneDockLengthRule(var maxLength: Int) : DefaultServiceAreaMilestoneRule {
    override fun check(item: DefaultServiceAreaMilestone): MutableList<String> {
        val size = item.dock.length
        if (size > maxLength) {
            return mutableListOf<String>("dock is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultServiceAreaMilestoneThresholdRangeRule(
    var min: Double,
    var max: Double,
) : DefaultServiceAreaMilestoneRule {
    override fun check(item: DefaultServiceAreaMilestone): MutableList<String> {
        val value = item.threshold
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("threshold $value is below $min")
        }
        if (value > max) {
            problems.add("threshold $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a default service area milestone. */
data class DefaultServiceAreaMilestoneValidator(var rules: MutableList<DefaultServiceAreaMilestoneRule>) {
    fun validate(item: DefaultServiceAreaMilestone): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefaultServiceAreaMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultServiceAreaMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultServiceAreaMilestoneValidator(): DefaultServiceAreaMilestoneValidator {
    return DefaultServiceAreaMilestoneValidator(
        mutableListOf<DefaultServiceAreaMilestoneRule>(
            DefaultServiceAreaMilestoneSequenceRangeRule(64, 80),
            DefaultServiceAreaMilestoneTitleLengthRule(3600),
            DefaultServiceAreaMilestoneGraceDaysRangeRule(255, 1000),
            DefaultServiceAreaMilestoneDockLengthRule(86400),
            DefaultServiceAreaMilestoneThresholdRangeRule(7.3, 811.4),
        ),
    )
}
