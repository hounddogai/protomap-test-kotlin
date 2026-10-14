package com.acme.transport.customs.clearance.variance

import com.acme.transport.kit.text.normalize

/** A rule a strict clearance group must pass. Check returns its problems, or none. */
interface StrictClearanceGroupRule {
    fun check(item: StrictClearanceGroup): MutableList<String>
}

data class StrictClearanceGroupAllowedNameRule(var allowed: MutableList<String>) : StrictClearanceGroupRule {
    override fun check(item: StrictClearanceGroup): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class StrictClearanceGroupLabelLengthRule(var maxLength: Int) : StrictClearanceGroupRule {
    override fun check(item: StrictClearanceGroup): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictClearanceGroupEtaMinutesRangeRule(var min: Int, var max: Int) : StrictClearanceGroupRule {
    override fun check(item: StrictClearanceGroup): MutableList<String> {
        val value = item.etaMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("eta minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class StrictClearanceGroupCarrierLengthRule(var maxLength: Int) : StrictClearanceGroupRule {
    override fun check(item: StrictClearanceGroup): MutableList<String> {
        val size = item.carrier.length
        if (size > maxLength) {
            return mutableListOf<String>("carrier is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict clearance group. */
data class StrictClearanceGroupValidator(var rules: MutableList<StrictClearanceGroupRule>) {
    fun validate(item: StrictClearanceGroup): MutableList<String> {
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

    fun requireValid(item: StrictClearanceGroup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictClearanceGroup>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictClearanceGroupValidator(): StrictClearanceGroupValidator {
    return StrictClearanceGroupValidator(
        mutableListOf<StrictClearanceGroupRule>(
            StrictClearanceGroupAllowedNameRule(mutableListOf<String>("seasonal", "regional")),
            StrictClearanceGroupLabelLengthRule(32),
            StrictClearanceGroupEtaMinutesRangeRule(80, 1000),
            StrictClearanceGroupCarrierLengthRule(255),
        ),
    )
}
