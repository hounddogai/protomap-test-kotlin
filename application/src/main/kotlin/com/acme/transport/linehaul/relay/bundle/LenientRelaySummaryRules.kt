package com.acme.transport.linehaul.relay.bundle

/** A rule a lenient relay summary must pass. Check returns its problems, or none. */
interface LenientRelaySummaryRule {
    fun check(item: LenientRelaySummary): MutableList<String>
}

data class LenientRelaySummaryDiscountRangeRule(var min: Double, var max: Double) : LenientRelaySummaryRule {
    override fun check(item: LenientRelaySummary): MutableList<String> {
        val value = item.discount
        if (value < min || value > max) {
            return mutableListOf<String>("discount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LenientRelaySummaryTitleLengthRule(var maxLength: Int) : LenientRelaySummaryRule {
    override fun check(item: LenientRelaySummary): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientRelaySummaryRequiredTitleRule(var message: String) : LenientRelaySummaryRule {
    override fun check(item: LenientRelaySummary): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient relay summary. */
data class LenientRelaySummaryValidator(var rules: MutableList<LenientRelaySummaryRule>) {
    fun validate(item: LenientRelaySummary): MutableList<String> {
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

    fun requireValid(item: LenientRelaySummary) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientRelaySummary>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientRelaySummaryValidator(): LenientRelaySummaryValidator {
    return LenientRelaySummaryValidator(
        mutableListOf<LenientRelaySummaryRule>(
            LenientRelaySummaryDiscountRangeRule(4.1, 4531.3),
            LenientRelaySummaryTitleLengthRule(64),
            LenientRelaySummaryRequiredTitleRule("lenient relay summary needs an entry"),
        ),
    )
}
