package com.acme.transport.emissions.idling.batch

/** A rule a adaptive idling ledger must pass. Check returns its problems, or none. */
interface AdaptiveIdlingLedgerRule {
    fun check(item: AdaptiveIdlingLedger): MutableList<String>
}

data class AdaptiveIdlingLedgerAttemptsRangeRule(var min: Int, var max: Int) : AdaptiveIdlingLedgerRule {
    override fun check(item: AdaptiveIdlingLedger): MutableList<String> {
        val value = item.attempts
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("attempts $value is below $min")
        }
        if (value > max) {
            problems.add("attempts $value is above $max")
        }
        return problems
    }
}

data class AdaptiveIdlingLedgerRequiredLabelRule(var message: String) : AdaptiveIdlingLedgerRule {
    override fun check(item: AdaptiveIdlingLedger): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveIdlingLedgerUnitLengthRule(var maxLength: Int) : AdaptiveIdlingLedgerRule {
    override fun check(item: AdaptiveIdlingLedger): MutableList<String> {
        val size = item.unit.length
        if (size > maxLength) {
            return mutableListOf<String>("unit is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveIdlingLedgerRequiredDescriptionRule(var message: String) : AdaptiveIdlingLedgerRule {
    override fun check(item: AdaptiveIdlingLedger): MutableList<String> {
        if (item.description.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveIdlingLedgerCodePrefixRule(var prefix: String) : AdaptiveIdlingLedgerRule {
    override fun check(item: AdaptiveIdlingLedger): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveIdlingLedgerDescriptionLengthRule(var maxLength: Int) : AdaptiveIdlingLedgerRule {
    override fun check(item: AdaptiveIdlingLedger): MutableList<String> {
        val size = item.description.length
        if (size > maxLength) {
            return mutableListOf<String>("description is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive idling ledger. */
data class AdaptiveIdlingLedgerValidator(var rules: MutableList<AdaptiveIdlingLedgerRule>) {
    fun validate(item: AdaptiveIdlingLedger): MutableList<String> {
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

    fun requireValid(item: AdaptiveIdlingLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveIdlingLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveIdlingLedgerValidator(): AdaptiveIdlingLedgerValidator {
    return AdaptiveIdlingLedgerValidator(
        mutableListOf<AdaptiveIdlingLedgerRule>(
            AdaptiveIdlingLedgerAttemptsRangeRule(120, 1000),
            AdaptiveIdlingLedgerRequiredLabelRule("adaptive idling ledger needs this field"),
            AdaptiveIdlingLedgerUnitLengthRule(120),
            AdaptiveIdlingLedgerRequiredDescriptionRule("adaptive idling ledger needs this field"),
            AdaptiveIdlingLedgerCodePrefixRule("Q"),
            AdaptiveIdlingLedgerDescriptionLengthRule(3600),
        ),
    )
}
