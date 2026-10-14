package com.acme.transport.freightpay.ratecard.target

/** A rule a manual rate card summary must pass. Check returns its problems, or none. */
interface ManualRateCardSummaryRule {
    fun check(item: ManualRateCardSummary): MutableList<String>
}

data class ManualRateCardSummaryCodePrefixRule(var prefix: String) : ManualRateCardSummaryRule {
    override fun check(item: ManualRateCardSummary): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ManualRateCardSummaryLabelLengthRule(var maxLength: Int) : ManualRateCardSummaryRule {
    override fun check(item: ManualRateCardSummary): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualRateCardSummaryRequiredDepotRule(var message: String) : ManualRateCardSummaryRule {
    override fun check(item: ManualRateCardSummary): MutableList<String> {
        if (item.depot.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualRateCardSummaryRequiredLabelRule(var message: String) : ManualRateCardSummaryRule {
    override fun check(item: ManualRateCardSummary): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualRateCardSummaryRateRangeRule(var min: Double, var max: Double) : ManualRateCardSummaryRule {
    override fun check(item: ManualRateCardSummary): MutableList<String> {
        val value = item.rate
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("rate $value is below $min")
        }
        if (value > max) {
            problems.add("rate $value is above $max")
        }
        return problems
    }
}

data class ManualRateCardSummaryRequiredCodeRule(var message: String) : ManualRateCardSummaryRule {
    override fun check(item: ManualRateCardSummary): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual rate card summary. */
data class ManualRateCardSummaryValidator(var rules: MutableList<ManualRateCardSummaryRule>) {
    fun validate(item: ManualRateCardSummary): MutableList<String> {
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

    fun requireValid(item: ManualRateCardSummary) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualRateCardSummary>): MutableMap<String, MutableList<String>> {
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

fun defaultManualRateCardSummaryValidator(): ManualRateCardSummaryValidator {
    return ManualRateCardSummaryValidator(
        mutableListOf<ManualRateCardSummaryRule>(
            ManualRateCardSummaryCodePrefixRule("X"),
            ManualRateCardSummaryLabelLengthRule(3600),
            ManualRateCardSummaryRequiredDepotRule("manual rate card summary needs an entry"),
            ManualRateCardSummaryRequiredLabelRule("manual rate card summary needs this field"),
            ManualRateCardSummaryRateRangeRule(1.2, 2120.6),
            ManualRateCardSummaryRequiredCodeRule("manual rate card summary needs a value"),
        ),
    )
}
