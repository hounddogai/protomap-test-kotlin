package com.acme.transport.emissions.fuelburn.recorditem

import com.acme.transport.kit.text.normalize

/** A rule a fuel burn record must pass. Check returns its problems, or none. */
interface FuelBurnRecordRule {
    fun check(item: FuelBurnRecord): MutableList<String>
}

data class FuelBurnRecordMaximumRangeRule(var min: Double, var max: Double) : FuelBurnRecordRule {
    override fun check(item: FuelBurnRecord): MutableList<String> {
        val value = item.maximum
        if (value < min || value > max) {
            return mutableListOf<String>("maximum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FuelBurnRecordAllowedTimezoneRule(var allowed: MutableList<String>) : FuelBurnRecordRule {
    override fun check(item: FuelBurnRecord): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.timezone))) {
            return mutableListOf<String>("timezone ${item.timezone} is expired")
        }
        return mutableListOf<String>()
    }
}

data class FuelBurnRecordRequiredTitleRule(var message: String) : FuelBurnRecordRule {
    override fun check(item: FuelBurnRecord): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FuelBurnRecordDescriptionLengthRule(var maxLength: Int) : FuelBurnRecordRule {
    override fun check(item: FuelBurnRecord): MutableList<String> {
        val size = item.description.length
        if (size > maxLength) {
            return mutableListOf<String>("description is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fuel burn record. */
data class FuelBurnRecordValidator(var rules: MutableList<FuelBurnRecordRule>) {
    fun validate(item: FuelBurnRecord): MutableList<String> {
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

    fun requireValid(item: FuelBurnRecord) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FuelBurnRecord>): MutableMap<String, MutableList<String>> {
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

fun defaultFuelBurnRecordValidator(): FuelBurnRecordValidator {
    return FuelBurnRecordValidator(
        mutableListOf<FuelBurnRecordRule>(
            FuelBurnRecordMaximumRangeRule(5.6, 921.6),
            FuelBurnRecordAllowedTimezoneRule(mutableListOf<String>()),
            FuelBurnRecordRequiredTitleRule("fuel burn record needs this field"),
            FuelBurnRecordDescriptionLengthRule(1000),
        ),
    )
}
