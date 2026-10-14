package com.acme.transport.crewing.roster.preference

/** A rule a roster slot must pass. Check returns its problems, or none. */
interface RosterSlotRule {
    fun check(item: RosterSlot): MutableList<String>
}

data class RosterSlotRequiredLabelRule(var message: String) : RosterSlotRule {
    override fun check(item: RosterSlot): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RosterSlotCategoryLengthRule(var maxLength: Int) : RosterSlotRule {
    override fun check(item: RosterSlot): MutableList<String> {
        val size = item.category.length
        if (size > maxLength) {
            return mutableListOf<String>("category is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RosterSlotThresholdRangeRule(var min: Double, var max: Double) : RosterSlotRule {
    override fun check(item: RosterSlot): MutableList<String> {
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

data class RosterSlotReviewerLengthRule(var maxLength: Int) : RosterSlotRule {
    override fun check(item: RosterSlot): MutableList<String> {
        val size = item.reviewer.length
        if (size > maxLength) {
            return mutableListOf<String>("reviewer is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a roster slot. */
data class RosterSlotValidator(var rules: MutableList<RosterSlotRule>) {
    fun validate(item: RosterSlot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RosterSlot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RosterSlot>): MutableMap<String, MutableList<String>> {
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

fun defaultRosterSlotValidator(): RosterSlotValidator {
    return RosterSlotValidator(
        mutableListOf<RosterSlotRule>(
            RosterSlotRequiredLabelRule("roster slot needs an entry"),
            RosterSlotCategoryLengthRule(16),
            RosterSlotThresholdRangeRule(9.2, 2000.0),
            RosterSlotReviewerLengthRule(86400),
        ),
    )
}
