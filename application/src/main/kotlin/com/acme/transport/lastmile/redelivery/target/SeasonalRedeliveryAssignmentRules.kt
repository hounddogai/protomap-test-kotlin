package com.acme.transport.lastmile.redelivery.target

import com.acme.transport.kit.text.normalize

/** A rule a seasonal redelivery assignment must pass. Check returns its problems, or none. */
interface SeasonalRedeliveryAssignmentRule {
    fun check(item: SeasonalRedeliveryAssignment): MutableList<String>
}

data class SeasonalRedeliveryAssignmentNameLengthRule(var maxLength: Int) : SeasonalRedeliveryAssignmentRule {
    override fun check(item: SeasonalRedeliveryAssignment): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalRedeliveryAssignmentCarrierLengthRule(
    var maxLength: Int,
) : SeasonalRedeliveryAssignmentRule {
    override fun check(item: SeasonalRedeliveryAssignment): MutableList<String> {
        val size = item.carrier.length
        if (size > maxLength) {
            return mutableListOf<String>("carrier is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalRedeliveryAssignmentPalletsRangeRule(
    var min: Int,
    var max: Int,
) : SeasonalRedeliveryAssignmentRule {
    override fun check(item: SeasonalRedeliveryAssignment): MutableList<String> {
        val value = item.pallets
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("pallets $value is below $min")
        }
        if (value > max) {
            problems.add("pallets $value is above $max")
        }
        return problems
    }
}

data class SeasonalRedeliveryAssignmentAllowedCodeRule(
    var allowed: MutableList<String>,
) : SeasonalRedeliveryAssignmentRule {
    override fun check(item: SeasonalRedeliveryAssignment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is unknown")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal redelivery assignment. */
data class SeasonalRedeliveryAssignmentValidator(var rules: MutableList<SeasonalRedeliveryAssignmentRule>) {
    fun validate(item: SeasonalRedeliveryAssignment): MutableList<String> {
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

    fun requireValid(item: SeasonalRedeliveryAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalRedeliveryAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalRedeliveryAssignmentValidator(): SeasonalRedeliveryAssignmentValidator {
    return SeasonalRedeliveryAssignmentValidator(
        mutableListOf<SeasonalRedeliveryAssignmentRule>(
            SeasonalRedeliveryAssignmentNameLengthRule(86400),
            SeasonalRedeliveryAssignmentCarrierLengthRule(3600),
            SeasonalRedeliveryAssignmentPalletsRangeRule(32, 255),
            SeasonalRedeliveryAssignmentAllowedCodeRule(mutableListOf<String>()),
        ),
    )
}
