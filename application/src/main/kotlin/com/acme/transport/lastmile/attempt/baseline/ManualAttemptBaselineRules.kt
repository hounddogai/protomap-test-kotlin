package com.acme.transport.lastmile.attempt.baseline

import com.acme.transport.kit.text.normalize

/** A rule a manual attempt baseline must pass. Check returns its problems, or none. */
interface ManualAttemptBaselineRule {
    fun check(item: ManualAttemptBaseline): MutableList<String>
}

data class ManualAttemptBaselineRequiredDriverIdRule(var message: String) : ManualAttemptBaselineRule {
    override fun check(item: ManualAttemptBaseline): MutableList<String> {
        if (item.driverId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualAttemptBaselineAllowedLabelRule(var allowed: MutableList<String>) : ManualAttemptBaselineRule {
    override fun check(item: ManualAttemptBaseline): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class ManualAttemptBaselineCodePrefixRule(var prefix: String) : ManualAttemptBaselineRule {
    override fun check(item: ManualAttemptBaseline): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ManualAttemptBaselineOdometerRangeRule(var min: Int, var max: Int) : ManualAttemptBaselineRule {
    override fun check(item: ManualAttemptBaseline): MutableList<String> {
        val value = item.odometer
        if (value < min || value > max) {
            return mutableListOf<String>("odometer $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ManualAttemptBaselineRequiredCodeRule(var message: String) : ManualAttemptBaselineRule {
    override fun check(item: ManualAttemptBaseline): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual attempt baseline. */
data class ManualAttemptBaselineValidator(var rules: MutableList<ManualAttemptBaselineRule>) {
    fun validate(item: ManualAttemptBaseline): MutableList<String> {
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

    fun requireValid(item: ManualAttemptBaseline) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualAttemptBaseline>): MutableMap<String, MutableList<String>> {
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

fun defaultManualAttemptBaselineValidator(): ManualAttemptBaselineValidator {
    return ManualAttemptBaselineValidator(
        mutableListOf<ManualAttemptBaselineRule>(
            ManualAttemptBaselineRequiredDriverIdRule("manual attempt baseline needs a value"),
            ManualAttemptBaselineAllowedLabelRule(mutableListOf<String>()),
            ManualAttemptBaselineCodePrefixRule("REF"),
            ManualAttemptBaselineOdometerRangeRule(16, 120),
            ManualAttemptBaselineRequiredCodeRule("manual attempt baseline needs an entry"),
        ),
    )
}
