package com.acme.transport.linehaul.backhaul.milestone

/** A rule a backhaul milestone must pass. Check returns its problems, or none. */
interface BackhaulMilestoneRule {
    fun check(item: BackhaulMilestone): MutableList<String>
}

data class BackhaulMilestoneRequiredLabelRule(var message: String) : BackhaulMilestoneRule {
    override fun check(item: BackhaulMilestone): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BackhaulMilestoneLabelLengthRule(var maxLength: Int) : BackhaulMilestoneRule {
    override fun check(item: BackhaulMilestone): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BackhaulMilestoneRequiredDepotRule(var message: String) : BackhaulMilestoneRule {
    override fun check(item: BackhaulMilestone): MutableList<String> {
        if (item.depot.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BackhaulMilestoneBatchSizeRangeRule(var min: Int, var max: Int) : BackhaulMilestoneRule {
    override fun check(item: BackhaulMilestone): MutableList<String> {
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

data class BackhaulMilestoneEndsAtRangeRule(var min: Int, var max: Int) : BackhaulMilestoneRule {
    override fun check(item: BackhaulMilestone): MutableList<String> {
        val value = item.endsAt
        if (value < min || value > max) {
            return mutableListOf<String>("ends at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a backhaul milestone. */
data class BackhaulMilestoneValidator(var rules: MutableList<BackhaulMilestoneRule>) {
    fun validate(item: BackhaulMilestone): MutableList<String> {
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

    fun requireValid(item: BackhaulMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BackhaulMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultBackhaulMilestoneValidator(): BackhaulMilestoneValidator {
    return BackhaulMilestoneValidator(
        mutableListOf<BackhaulMilestoneRule>(
            BackhaulMilestoneRequiredLabelRule("backhaul milestone needs an entry"),
            BackhaulMilestoneLabelLengthRule(80),
            BackhaulMilestoneRequiredDepotRule("backhaul milestone needs this field"),
            BackhaulMilestoneBatchSizeRangeRule(1000, 86400),
            BackhaulMilestoneEndsAtRangeRule(80, 86400),
        ),
    )
}
