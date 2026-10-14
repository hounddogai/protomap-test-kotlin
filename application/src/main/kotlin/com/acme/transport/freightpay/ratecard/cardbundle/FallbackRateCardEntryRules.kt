package com.acme.transport.freightpay.ratecard.cardbundle

/** A rule a fallback rate card entry must pass. Check returns its problems, or none. */
interface FallbackRateCardEntryRule {
    fun check(item: FallbackRateCardEntry): MutableList<String>
}

data class FallbackRateCardEntryLocaleLengthRule(var maxLength: Int) : FallbackRateCardEntryRule {
    override fun check(item: FallbackRateCardEntry): MutableList<String> {
        val size = item.locale.length
        if (size > maxLength) {
            return mutableListOf<String>("locale is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackRateCardEntryRequiredLabelRule(var message: String) : FallbackRateCardEntryRule {
    override fun check(item: FallbackRateCardEntry): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackRateCardEntryRequiredCodeRule(var message: String) : FallbackRateCardEntryRule {
    override fun check(item: FallbackRateCardEntry): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback rate card entry. */
data class FallbackRateCardEntryValidator(var rules: MutableList<FallbackRateCardEntryRule>) {
    fun validate(item: FallbackRateCardEntry): MutableList<String> {
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

    fun requireValid(item: FallbackRateCardEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackRateCardEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackRateCardEntryValidator(): FallbackRateCardEntryValidator {
    return FallbackRateCardEntryValidator(
        mutableListOf<FallbackRateCardEntryRule>(
            FallbackRateCardEntryLocaleLengthRule(32),
            FallbackRateCardEntryRequiredLabelRule("fallback rate card entry needs an entry"),
            FallbackRateCardEntryRequiredCodeRule("fallback rate card entry needs a value"),
        ),
    )
}
