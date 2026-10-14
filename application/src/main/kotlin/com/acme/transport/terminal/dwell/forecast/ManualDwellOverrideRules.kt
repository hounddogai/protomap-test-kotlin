package com.acme.transport.terminal.dwell.forecast

import com.acme.transport.kit.text.normalize

/** A rule a manual dwell override must pass. Check returns its problems, or none. */
interface ManualDwellOverrideRule {
    fun check(item: ManualDwellOverride): MutableList<String>
}

data class ManualDwellOverrideMaximumRangeRule(var min: Double, var max: Double) : ManualDwellOverrideRule {
    override fun check(item: ManualDwellOverride): MutableList<String> {
        val value = item.maximum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("maximum $value is below $min")
        }
        if (value > max) {
            problems.add("maximum $value is above $max")
        }
        return problems
    }
}

data class ManualDwellOverrideRequiredNameRule(var message: String) : ManualDwellOverrideRule {
    override fun check(item: ManualDwellOverride): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualDwellOverrideFloorValueRangeRule(var min: Double, var max: Double) : ManualDwellOverrideRule {
    override fun check(item: ManualDwellOverride): MutableList<String> {
        val value = item.floorValue
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("floor value $value is below $min")
        }
        if (value > max) {
            problems.add("floor value $value is above $max")
        }
        return problems
    }
}

data class ManualDwellOverrideAllowedNameRule(var allowed: MutableList<String>) : ManualDwellOverrideRule {
    override fun check(item: ManualDwellOverride): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is duplicated")
        }
        return mutableListOf<String>()
    }
}

data class ManualDwellOverrideDiscountRangeRule(var min: Double, var max: Double) : ManualDwellOverrideRule {
    override fun check(item: ManualDwellOverride): MutableList<String> {
        val value = item.discount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("discount $value is below $min")
        }
        if (value > max) {
            problems.add("discount $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a manual dwell override. */
data class ManualDwellOverrideValidator(var rules: MutableList<ManualDwellOverrideRule>) {
    fun validate(item: ManualDwellOverride): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ManualDwellOverride) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualDwellOverride>): MutableMap<String, MutableList<String>> {
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

fun defaultManualDwellOverrideValidator(): ManualDwellOverrideValidator {
    return ManualDwellOverrideValidator(
        mutableListOf<ManualDwellOverrideRule>(
            ManualDwellOverrideMaximumRangeRule(9.8, 3250.3),
            ManualDwellOverrideRequiredNameRule("manual dwell override needs an entry"),
            ManualDwellOverrideFloorValueRangeRule(5.3, 1909.6),
            ManualDwellOverrideAllowedNameRule(mutableListOf<String>("strict", "global")),
            ManualDwellOverrideDiscountRangeRule(4.2, 1117.7),
        ),
    )
}
