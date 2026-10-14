package com.acme.transport.fleetops.serviceplan.assignment

import com.acme.transport.kit.text.normalize

/** A rule a seasonal service plan record must pass. Check returns its problems, or none. */
interface SeasonalServicePlanRecordRule {
    fun check(item: SeasonalServicePlanRecord): MutableList<String>
}

data class SeasonalServicePlanRecordGraceDaysRangeRule(
    var min: Int,
    var max: Int,
) : SeasonalServicePlanRecordRule {
    override fun check(item: SeasonalServicePlanRecord): MutableList<String> {
        val value = item.graceDays
        if (value < min || value > max) {
            return mutableListOf<String>("grace days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalServicePlanRecordStopCountRangeRule(
    var min: Int,
    var max: Int,
) : SeasonalServicePlanRecordRule {
    override fun check(item: SeasonalServicePlanRecord): MutableList<String> {
        val value = item.stopCount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("stop count $value is below $min")
        }
        if (value > max) {
            problems.add("stop count $value is above $max")
        }
        return problems
    }
}

data class SeasonalServicePlanRecordAllowedNameRule(
    var allowed: MutableList<String>,
) : SeasonalServicePlanRecordRule {
    override fun check(item: SeasonalServicePlanRecord): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalServicePlanRecordNameLengthRule(var maxLength: Int) : SeasonalServicePlanRecordRule {
    override fun check(item: SeasonalServicePlanRecord): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalServicePlanRecordRequiredNameRule(var message: String) : SeasonalServicePlanRecordRule {
    override fun check(item: SeasonalServicePlanRecord): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalServicePlanRecordCarrierLengthRule(var maxLength: Int) : SeasonalServicePlanRecordRule {
    override fun check(item: SeasonalServicePlanRecord): MutableList<String> {
        val size = item.carrier.length
        if (size > maxLength) {
            return mutableListOf<String>("carrier is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal service plan record. */
data class SeasonalServicePlanRecordValidator(var rules: MutableList<SeasonalServicePlanRecordRule>) {
    fun validate(item: SeasonalServicePlanRecord): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SeasonalServicePlanRecord) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalServicePlanRecord>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalServicePlanRecordValidator(): SeasonalServicePlanRecordValidator {
    return SeasonalServicePlanRecordValidator(
        mutableListOf<SeasonalServicePlanRecordRule>(
            SeasonalServicePlanRecordGraceDaysRangeRule(16, 1000),
            SeasonalServicePlanRecordStopCountRangeRule(16, 64),
            SeasonalServicePlanRecordAllowedNameRule(mutableListOf<String>("seasonal")),
            SeasonalServicePlanRecordNameLengthRule(86400),
            SeasonalServicePlanRecordRequiredNameRule("seasonal service plan record needs an entry"),
            SeasonalServicePlanRecordCarrierLengthRule(16),
        ),
    )
}
