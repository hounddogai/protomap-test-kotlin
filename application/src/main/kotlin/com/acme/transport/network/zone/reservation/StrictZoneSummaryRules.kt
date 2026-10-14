package com.acme.transport.network.zone.reservation

/** A rule a strict zone summary must pass. Check returns its problems, or none. */
interface StrictZoneSummaryRule {
    fun check(item: StrictZoneSummary): MutableList<String>
}

data class StrictZoneSummaryLabelLengthRule(var maxLength: Int) : StrictZoneSummaryRule {
    override fun check(item: StrictZoneSummary): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictZoneSummaryRequiredLabelRule(var message: String) : StrictZoneSummaryRule {
    override fun check(item: StrictZoneSummary): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictZoneSummaryRequiredLaneRule(var message: String) : StrictZoneSummaryRule {
    override fun check(item: StrictZoneSummary): MutableList<String> {
        if (item.lane.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictZoneSummaryLaneLengthRule(var maxLength: Int) : StrictZoneSummaryRule {
    override fun check(item: StrictZoneSummary): MutableList<String> {
        val size = item.lane.length
        if (size > maxLength) {
            return mutableListOf<String>("lane is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictZoneSummaryCostRangeRule(var min: Double, var max: Double) : StrictZoneSummaryRule {
    override fun check(item: StrictZoneSummary): MutableList<String> {
        val value = item.cost
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("cost $value is below $min")
        }
        if (value > max) {
            problems.add("cost $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a strict zone summary. */
data class StrictZoneSummaryValidator(var rules: MutableList<StrictZoneSummaryRule>) {
    fun validate(item: StrictZoneSummary): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: StrictZoneSummary) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictZoneSummary>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictZoneSummaryValidator(): StrictZoneSummaryValidator {
    return StrictZoneSummaryValidator(
        mutableListOf<StrictZoneSummaryRule>(
            StrictZoneSummaryLabelLengthRule(80),
            StrictZoneSummaryRequiredLabelRule("strict zone summary needs a value"),
            StrictZoneSummaryRequiredLaneRule("strict zone summary needs a value"),
            StrictZoneSummaryLaneLengthRule(255),
            StrictZoneSummaryCostRangeRule(1.6, 4979.1),
        ),
    )
}
