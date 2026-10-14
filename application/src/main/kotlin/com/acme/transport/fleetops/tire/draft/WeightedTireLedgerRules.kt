package com.acme.transport.fleetops.tire.draft

/** A rule a weighted tire ledger must pass. Check returns its problems, or none. */
interface WeightedTireLedgerRule {
    fun check(item: WeightedTireLedger): MutableList<String>
}

data class WeightedTireLedgerCodeLengthRule(var maxLength: Int) : WeightedTireLedgerRule {
    override fun check(item: WeightedTireLedger): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedTireLedgerRequiredCodeRule(var message: String) : WeightedTireLedgerRule {
    override fun check(item: WeightedTireLedger): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedTireLedgerDurationMinutesRangeRule(var min: Int, var max: Int) : WeightedTireLedgerRule {
    override fun check(item: WeightedTireLedger): MutableList<String> {
        val value = item.durationMinutes
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("duration minutes $value is below $min")
        }
        if (value > max) {
            problems.add("duration minutes $value is above $max")
        }
        return problems
    }
}

data class WeightedTireLedgerRequiredCountryRule(var message: String) : WeightedTireLedgerRule {
    override fun check(item: WeightedTireLedger): MutableList<String> {
        if (item.country.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted tire ledger. */
data class WeightedTireLedgerValidator(var rules: MutableList<WeightedTireLedgerRule>) {
    fun validate(item: WeightedTireLedger): MutableList<String> {
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

    fun requireValid(item: WeightedTireLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedTireLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedTireLedgerValidator(): WeightedTireLedgerValidator {
    return WeightedTireLedgerValidator(
        mutableListOf<WeightedTireLedgerRule>(
            WeightedTireLedgerCodeLengthRule(1000),
            WeightedTireLedgerRequiredCodeRule("weighted tire ledger needs this field"),
            WeightedTireLedgerDurationMinutesRangeRule(16, 255),
            WeightedTireLedgerRequiredCountryRule("weighted tire ledger needs an entry"),
        ),
    )
}
