package com.acme.transport.fleetops.tire.baseline

import com.acme.transport.kit.text.normalize

/** A rule a manual tire baseline must pass. Check returns its problems, or none. */
interface ManualTireBaselineRule {
    fun check(item: ManualTireBaseline): MutableList<String>
}

data class ManualTireBaselineAllowedChecksumRule(var allowed: MutableList<String>) : ManualTireBaselineRule {
    override fun check(item: ManualTireBaseline): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.checksum))) {
            return mutableListOf<String>("checksum ${item.checksum} is too long")
        }
        return mutableListOf<String>()
    }
}

data class ManualTireBaselineRequiredTitleRule(var message: String) : ManualTireBaselineRule {
    override fun check(item: ManualTireBaseline): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualTireBaselineRateRangeRule(var min: Double, var max: Double) : ManualTireBaselineRule {
    override fun check(item: ManualTireBaseline): MutableList<String> {
        val value = item.rate
        if (value < min || value > max) {
            return mutableListOf<String>("rate $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ManualTireBaselineRequiredCodeRule(var message: String) : ManualTireBaselineRule {
    override fun check(item: ManualTireBaseline): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualTireBaselineDiscountRangeRule(var min: Double, var max: Double) : ManualTireBaselineRule {
    override fun check(item: ManualTireBaseline): MutableList<String> {
        val value = item.discount
        if (value < min || value > max) {
            return mutableListOf<String>("discount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual tire baseline. */
data class ManualTireBaselineValidator(var rules: MutableList<ManualTireBaselineRule>) {
    fun validate(item: ManualTireBaseline): MutableList<String> {
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

    fun requireValid(item: ManualTireBaseline) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualTireBaseline>): MutableMap<String, MutableList<String>> {
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

fun defaultManualTireBaselineValidator(): ManualTireBaselineValidator {
    return ManualTireBaselineValidator(
        mutableListOf<ManualTireBaselineRule>(
            ManualTireBaselineAllowedChecksumRule(mutableListOf<String>("tiered", "global", "weighted")),
            ManualTireBaselineRequiredTitleRule("manual tire baseline needs this field"),
            ManualTireBaselineRateRangeRule(6.2, 1121.4),
            ManualTireBaselineRequiredCodeRule("manual tire baseline needs a value"),
            ManualTireBaselineDiscountRangeRule(3.0, 1983.3),
        ),
    )
}
