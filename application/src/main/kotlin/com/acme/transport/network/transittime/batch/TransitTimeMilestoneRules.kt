package com.acme.transport.network.transittime.batch

/** A rule a transit time milestone must pass. Check returns its problems, or none. */
interface TransitTimeMilestoneRule {
    fun check(item: TransitTimeMilestone): MutableList<String>
}

data class TransitTimeMilestoneOdometerRangeRule(var min: Int, var max: Int) : TransitTimeMilestoneRule {
    override fun check(item: TransitTimeMilestone): MutableList<String> {
        val value = item.odometer
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("odometer $value is below $min")
        }
        if (value > max) {
            problems.add("odometer $value is above $max")
        }
        return problems
    }
}

data class TransitTimeMilestoneCodePrefixRule(var prefix: String) : TransitTimeMilestoneRule {
    override fun check(item: TransitTimeMilestone): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TransitTimeMilestoneRequiredCodeRule(var message: String) : TransitTimeMilestoneRule {
    override fun check(item: TransitTimeMilestone): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TransitTimeMilestoneDurationMinutesRangeRule(var min: Int, var max: Int) : TransitTimeMilestoneRule {
    override fun check(item: TransitTimeMilestone): MutableList<String> {
        val value = item.durationMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("duration minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TransitTimeMilestoneRequiredCategoryRule(var message: String) : TransitTimeMilestoneRule {
    override fun check(item: TransitTimeMilestone): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TransitTimeMilestoneRequiredLabelRule(var message: String) : TransitTimeMilestoneRule {
    override fun check(item: TransitTimeMilestone): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a transit time milestone. */
data class TransitTimeMilestoneValidator(var rules: MutableList<TransitTimeMilestoneRule>) {
    fun validate(item: TransitTimeMilestone): MutableList<String> {
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

    fun requireValid(item: TransitTimeMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TransitTimeMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultTransitTimeMilestoneValidator(): TransitTimeMilestoneValidator {
    return TransitTimeMilestoneValidator(
        mutableListOf<TransitTimeMilestoneRule>(
            TransitTimeMilestoneOdometerRangeRule(120, 86400),
            TransitTimeMilestoneCodePrefixRule("Q"),
            TransitTimeMilestoneRequiredCodeRule("transit time milestone needs a value"),
            TransitTimeMilestoneDurationMinutesRangeRule(120, 255),
            TransitTimeMilestoneRequiredCategoryRule("transit time milestone needs an entry"),
            TransitTimeMilestoneRequiredLabelRule("transit time milestone needs an entry"),
        ),
    )
}
