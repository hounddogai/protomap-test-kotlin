package com.acme.transport.network.transittime.timedigest

/** A rule a capped transit time summary must pass. Check returns its problems, or none. */
interface CappedTransitTimeSummaryRule {
    fun check(item: CappedTransitTimeSummary): MutableList<String>
}

data class CappedTransitTimeSummaryEndsAtRangeRule(var min: Int, var max: Int) : CappedTransitTimeSummaryRule {
    override fun check(item: CappedTransitTimeSummary): MutableList<String> {
        val value = item.endsAt
        if (value < min || value > max) {
            return mutableListOf<String>("ends at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CappedTransitTimeSummaryExternalRefLengthRule(var maxLength: Int) : CappedTransitTimeSummaryRule {
    override fun check(item: CappedTransitTimeSummary): MutableList<String> {
        val size = item.externalRef.length
        if (size > maxLength) {
            return mutableListOf<String>("external ref is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedTransitTimeSummaryCodeLengthRule(var maxLength: Int) : CappedTransitTimeSummaryRule {
    override fun check(item: CappedTransitTimeSummary): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped transit time summary. */
data class CappedTransitTimeSummaryValidator(var rules: MutableList<CappedTransitTimeSummaryRule>) {
    fun validate(item: CappedTransitTimeSummary): MutableList<String> {
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

    fun requireValid(item: CappedTransitTimeSummary) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedTransitTimeSummary>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedTransitTimeSummaryValidator(): CappedTransitTimeSummaryValidator {
    return CappedTransitTimeSummaryValidator(
        mutableListOf<CappedTransitTimeSummaryRule>(
            CappedTransitTimeSummaryEndsAtRangeRule(16, 1000),
            CappedTransitTimeSummaryExternalRefLengthRule(1000),
            CappedTransitTimeSummaryCodeLengthRule(80),
        ),
    )
}
