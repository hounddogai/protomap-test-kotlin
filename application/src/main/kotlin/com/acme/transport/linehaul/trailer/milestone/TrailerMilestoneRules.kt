package com.acme.transport.linehaul.trailer.milestone

/** A rule a trailer milestone must pass. Check returns its problems, or none. */
interface TrailerMilestoneRule {
    fun check(item: TrailerMilestone): MutableList<String>
}

data class TrailerMilestoneFuelLitersRangeRule(var min: Double, var max: Double) : TrailerMilestoneRule {
    override fun check(item: TrailerMilestone): MutableList<String> {
        val value = item.fuelLiters
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("fuel liters $value is below $min")
        }
        if (value > max) {
            problems.add("fuel liters $value is above $max")
        }
        return problems
    }
}

data class TrailerMilestoneLocaleLengthRule(var maxLength: Int) : TrailerMilestoneRule {
    override fun check(item: TrailerMilestone): MutableList<String> {
        val size = item.locale.length
        if (size > maxLength) {
            return mutableListOf<String>("locale is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TrailerMilestoneShiftCodeLengthRule(var maxLength: Int) : TrailerMilestoneRule {
    override fun check(item: TrailerMilestone): MutableList<String> {
        val size = item.shiftCode.length
        if (size > maxLength) {
            return mutableListOf<String>("shift code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TrailerMilestoneRequiredLocaleRule(var message: String) : TrailerMilestoneRule {
    override fun check(item: TrailerMilestone): MutableList<String> {
        if (item.locale.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TrailerMilestoneCodeLengthRule(var maxLength: Int) : TrailerMilestoneRule {
    override fun check(item: TrailerMilestone): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a trailer milestone. */
data class TrailerMilestoneValidator(var rules: MutableList<TrailerMilestoneRule>) {
    fun validate(item: TrailerMilestone): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TrailerMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TrailerMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultTrailerMilestoneValidator(): TrailerMilestoneValidator {
    return TrailerMilestoneValidator(
        mutableListOf<TrailerMilestoneRule>(
            TrailerMilestoneFuelLitersRangeRule(5.6, 3862.3),
            TrailerMilestoneLocaleLengthRule(32),
            TrailerMilestoneShiftCodeLengthRule(255),
            TrailerMilestoneRequiredLocaleRule("trailer milestone needs an entry"),
            TrailerMilestoneCodeLengthRule(64),
        ),
    )
}
