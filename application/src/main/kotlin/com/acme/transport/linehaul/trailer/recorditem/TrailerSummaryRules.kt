package com.acme.transport.linehaul.trailer.recorditem

/** A rule a trailer summary must pass. Check returns its problems, or none. */
interface TrailerSummaryRule {
    fun check(item: TrailerSummary): MutableList<String>
}

data class TrailerSummaryDriverIdLengthRule(var maxLength: Int) : TrailerSummaryRule {
    override fun check(item: TrailerSummary): MutableList<String> {
        val size = item.driverId.length
        if (size > maxLength) {
            return mutableListOf<String>("driver id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TrailerSummaryRequiredLaneRule(var message: String) : TrailerSummaryRule {
    override fun check(item: TrailerSummary): MutableList<String> {
        if (item.lane.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TrailerSummaryCeilingRangeRule(var min: Double, var max: Double) : TrailerSummaryRule {
    override fun check(item: TrailerSummary): MutableList<String> {
        val value = item.ceiling
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ceiling $value is below $min")
        }
        if (value > max) {
            problems.add("ceiling $value is above $max")
        }
        return problems
    }
}

data class TrailerSummaryScoreRangeRule(var min: Double, var max: Double) : TrailerSummaryRule {
    override fun check(item: TrailerSummary): MutableList<String> {
        val value = item.score
        if (value < min || value > max) {
            return mutableListOf<String>("score $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a trailer summary. */
data class TrailerSummaryValidator(var rules: MutableList<TrailerSummaryRule>) {
    fun validate(item: TrailerSummary): MutableList<String> {
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

    fun requireValid(item: TrailerSummary) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TrailerSummary>): MutableMap<String, MutableList<String>> {
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

fun defaultTrailerSummaryValidator(): TrailerSummaryValidator {
    return TrailerSummaryValidator(
        mutableListOf<TrailerSummaryRule>(
            TrailerSummaryDriverIdLengthRule(86400),
            TrailerSummaryRequiredLaneRule("trailer summary needs an entry"),
            TrailerSummaryCeilingRangeRule(2.1, 3198.7),
            TrailerSummaryScoreRangeRule(7.8, 3998.4),
        ),
    )
}
