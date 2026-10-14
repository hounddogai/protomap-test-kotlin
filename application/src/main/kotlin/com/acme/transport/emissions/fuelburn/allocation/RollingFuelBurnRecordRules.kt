package com.acme.transport.emissions.fuelburn.allocation

import com.acme.transport.kit.text.normalize

/** A rule a rolling fuel burn record must pass. Check returns its problems, or none. */
interface RollingFuelBurnRecordRule {
    fun check(item: RollingFuelBurnRecord): MutableList<String>
}

data class RollingFuelBurnRecordRequiredCodeRule(var message: String) : RollingFuelBurnRecordRule {
    override fun check(item: RollingFuelBurnRecord): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingFuelBurnRecordLoadKgRangeRule(var min: Double, var max: Double) : RollingFuelBurnRecordRule {
    override fun check(item: RollingFuelBurnRecord): MutableList<String> {
        val value = item.loadKg
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("load kg $value is below $min")
        }
        if (value > max) {
            problems.add("load kg $value is above $max")
        }
        return problems
    }
}

data class RollingFuelBurnRecordAllowedNameRule(var allowed: MutableList<String>) : RollingFuelBurnRecordRule {
    override fun check(item: RollingFuelBurnRecord): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is missing")
        }
        return mutableListOf<String>()
    }
}

data class RollingFuelBurnRecordNameLengthRule(var maxLength: Int) : RollingFuelBurnRecordRule {
    override fun check(item: RollingFuelBurnRecord): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingFuelBurnRecordThresholdRangeRule(
    var min: Double,
    var max: Double,
) : RollingFuelBurnRecordRule {
    override fun check(item: RollingFuelBurnRecord): MutableList<String> {
        val value = item.threshold
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("threshold $value is below $min")
        }
        if (value > max) {
            problems.add("threshold $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a rolling fuel burn record. */
data class RollingFuelBurnRecordValidator(var rules: MutableList<RollingFuelBurnRecordRule>) {
    fun validate(item: RollingFuelBurnRecord): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RollingFuelBurnRecord) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingFuelBurnRecord>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingFuelBurnRecordValidator(): RollingFuelBurnRecordValidator {
    return RollingFuelBurnRecordValidator(
        mutableListOf<RollingFuelBurnRecordRule>(
            RollingFuelBurnRecordRequiredCodeRule("rolling fuel burn record needs an entry"),
            RollingFuelBurnRecordLoadKgRangeRule(9.6, 642.2),
            RollingFuelBurnRecordAllowedNameRule(mutableListOf<String>()),
            RollingFuelBurnRecordNameLengthRule(16),
            RollingFuelBurnRecordThresholdRangeRule(5.3, 2313.2),
        ),
    )
}
