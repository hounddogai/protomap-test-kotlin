package com.acme.transport.fleetops.utilization.utilizationreservation

import com.acme.transport.kit.text.normalize

/** A rule a manual utilization reservation must pass. Check returns its problems, or none. */
interface ManualUtilizationReservationRule {
    fun check(item: ManualUtilizationReservation): MutableList<String>
}

data class ManualUtilizationReservationNameLengthRule(var maxLength: Int) : ManualUtilizationReservationRule {
    override fun check(item: ManualUtilizationReservation): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualUtilizationReservationAllowedExternalRefRule(
    var allowed: MutableList<String>,
) : ManualUtilizationReservationRule {
    override fun check(item: ManualUtilizationReservation): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.externalRef))) {
            return mutableListOf<String>("external ref ${item.externalRef} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class ManualUtilizationReservationRequiredCategoryRule(
    var message: String,
) : ManualUtilizationReservationRule {
    override fun check(item: ManualUtilizationReservation): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualUtilizationReservationRetriesRangeRule(
    var min: Int,
    var max: Int,
) : ManualUtilizationReservationRule {
    override fun check(item: ManualUtilizationReservation): MutableList<String> {
        val value = item.retries
        if (value < min || value > max) {
            return mutableListOf<String>("retries $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ManualUtilizationReservationRequiredCodeRule(
    var message: String,
) : ManualUtilizationReservationRule {
    override fun check(item: ManualUtilizationReservation): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual utilization reservation. */
data class ManualUtilizationReservationValidator(var rules: MutableList<ManualUtilizationReservationRule>) {
    fun validate(item: ManualUtilizationReservation): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ManualUtilizationReservation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualUtilizationReservation>): MutableMap<String, MutableList<String>> {
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

fun defaultManualUtilizationReservationValidator(): ManualUtilizationReservationValidator {
    return ManualUtilizationReservationValidator(
        mutableListOf<ManualUtilizationReservationRule>(
            ManualUtilizationReservationNameLengthRule(1000),
            ManualUtilizationReservationAllowedExternalRefRule(mutableListOf<String>("batched", "strict")),
            ManualUtilizationReservationRequiredCategoryRule("manual utilization reservation needs a value"),
            ManualUtilizationReservationRetriesRangeRule(32, 86400),
            ManualUtilizationReservationRequiredCodeRule("manual utilization reservation needs an entry"),
        ),
    )
}
