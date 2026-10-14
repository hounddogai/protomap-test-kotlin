package com.acme.transport.crewing.roster.score

/** A rule a fallback roster summary must pass. Check returns its problems, or none. */
interface FallbackRosterSummaryRule {
    fun check(item: FallbackRosterSummary): MutableList<String>
}

data class FallbackRosterSummarySequenceRangeRule(var min: Int, var max: Int) : FallbackRosterSummaryRule {
    override fun check(item: FallbackRosterSummary): MutableList<String> {
        val value = item.sequence
        if (value < min || value > max) {
            return mutableListOf<String>("sequence $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FallbackRosterSummaryRegionLengthRule(var maxLength: Int) : FallbackRosterSummaryRule {
    override fun check(item: FallbackRosterSummary): MutableList<String> {
        val size = item.region.length
        if (size > maxLength) {
            return mutableListOf<String>("region is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackRosterSummaryCodeLengthRule(var maxLength: Int) : FallbackRosterSummaryRule {
    override fun check(item: FallbackRosterSummary): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackRosterSummaryRequiredRegionRule(var message: String) : FallbackRosterSummaryRule {
    override fun check(item: FallbackRosterSummary): MutableList<String> {
        if (item.region.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackRosterSummaryRevisionRangeRule(var min: Int, var max: Int) : FallbackRosterSummaryRule {
    override fun check(item: FallbackRosterSummary): MutableList<String> {
        val value = item.revision
        if (value < min || value > max) {
            return mutableListOf<String>("revision $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback roster summary. */
data class FallbackRosterSummaryValidator(var rules: MutableList<FallbackRosterSummaryRule>) {
    fun validate(item: FallbackRosterSummary): MutableList<String> {
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

    fun requireValid(item: FallbackRosterSummary) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackRosterSummary>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackRosterSummaryValidator(): FallbackRosterSummaryValidator {
    return FallbackRosterSummaryValidator(
        mutableListOf<FallbackRosterSummaryRule>(
            FallbackRosterSummarySequenceRangeRule(16, 86400),
            FallbackRosterSummaryRegionLengthRule(86400),
            FallbackRosterSummaryCodeLengthRule(32),
            FallbackRosterSummaryRequiredRegionRule("fallback roster summary needs a value"),
            FallbackRosterSummaryRevisionRangeRule(16, 86400),
        ),
    )
}
