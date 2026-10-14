package com.acme.transport.crewing.roster.review

import com.acme.transport.kit.text.normalize

/** A rule a primary roster record must pass. Check returns its problems, or none. */
interface PrimaryRosterRecordRule {
    fun check(item: PrimaryRosterRecord): MutableList<String>
}

data class PrimaryRosterRecordRequiredCurrencyRule(var message: String) : PrimaryRosterRecordRule {
    override fun check(item: PrimaryRosterRecord): MutableList<String> {
        if (item.currency.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryRosterRecordLoadKgRangeRule(var min: Double, var max: Double) : PrimaryRosterRecordRule {
    override fun check(item: PrimaryRosterRecord): MutableList<String> {
        val value = item.loadKg
        if (value < min || value > max) {
            return mutableListOf<String>("load kg $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryRosterRecordBatchSizeRangeRule(var min: Int, var max: Int) : PrimaryRosterRecordRule {
    override fun check(item: PrimaryRosterRecord): MutableList<String> {
        val value = item.batchSize
        if (value < min || value > max) {
            return mutableListOf<String>("batch size $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryRosterRecordAllowedCurrencyRule(var allowed: MutableList<String>) : PrimaryRosterRecordRule {
    override fun check(item: PrimaryRosterRecord): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.currency))) {
            return mutableListOf<String>("currency ${item.currency} is missing")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary roster record. */
data class PrimaryRosterRecordValidator(var rules: MutableList<PrimaryRosterRecordRule>) {
    fun validate(item: PrimaryRosterRecord): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PrimaryRosterRecord) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryRosterRecord>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryRosterRecordValidator(): PrimaryRosterRecordValidator {
    return PrimaryRosterRecordValidator(
        mutableListOf<PrimaryRosterRecordRule>(
            PrimaryRosterRecordRequiredCurrencyRule("primary roster record needs this field"),
            PrimaryRosterRecordLoadKgRangeRule(4.2, 4230.5),
            PrimaryRosterRecordBatchSizeRangeRule(16, 3600),
            PrimaryRosterRecordAllowedCurrencyRule(mutableListOf<String>("global", "primary")),
        ),
    )
}
