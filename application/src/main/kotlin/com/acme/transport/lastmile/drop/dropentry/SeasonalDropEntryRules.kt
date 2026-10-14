package com.acme.transport.lastmile.drop.dropentry

/** A rule a seasonal drop entry must pass. Check returns its problems, or none. */
interface SeasonalDropEntryRule {
    fun check(item: SeasonalDropEntry): MutableList<String>
}

data class SeasonalDropEntryCodeLengthRule(var maxLength: Int) : SeasonalDropEntryRule {
    override fun check(item: SeasonalDropEntry): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalDropEntryRequiredSegmentRule(var message: String) : SeasonalDropEntryRule {
    override fun check(item: SeasonalDropEntry): MutableList<String> {
        if (item.segment.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalDropEntryCostRangeRule(var min: Double, var max: Double) : SeasonalDropEntryRule {
    override fun check(item: SeasonalDropEntry): MutableList<String> {
        val value = item.cost
        if (value < min || value > max) {
            return mutableListOf<String>("cost $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalDropEntryRequiredReviewerRule(var message: String) : SeasonalDropEntryRule {
    override fun check(item: SeasonalDropEntry): MutableList<String> {
        if (item.reviewer.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalDropEntryReviewerLengthRule(var maxLength: Int) : SeasonalDropEntryRule {
    override fun check(item: SeasonalDropEntry): MutableList<String> {
        val size = item.reviewer.length
        if (size > maxLength) {
            return mutableListOf<String>("reviewer is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalDropEntryRequiredNameRule(var message: String) : SeasonalDropEntryRule {
    override fun check(item: SeasonalDropEntry): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal drop entry. */
data class SeasonalDropEntryValidator(var rules: MutableList<SeasonalDropEntryRule>) {
    fun validate(item: SeasonalDropEntry): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SeasonalDropEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalDropEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalDropEntryValidator(): SeasonalDropEntryValidator {
    return SeasonalDropEntryValidator(
        mutableListOf<SeasonalDropEntryRule>(
            SeasonalDropEntryCodeLengthRule(16),
            SeasonalDropEntryRequiredSegmentRule("seasonal drop entry needs this field"),
            SeasonalDropEntryCostRangeRule(1.4, 3153.6),
            SeasonalDropEntryRequiredReviewerRule("seasonal drop entry needs a value"),
            SeasonalDropEntryReviewerLengthRule(16),
            SeasonalDropEntryRequiredNameRule("seasonal drop entry needs this field"),
        ),
    )
}
