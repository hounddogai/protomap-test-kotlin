package com.acme.transport.network.cutoff.cutofftarget

/** A rule a regional cutoff override must pass. Check returns its problems, or none. */
interface RegionalCutoffOverrideRule {
    fun check(item: RegionalCutoffOverride): MutableList<String>
}

data class RegionalCutoffOverrideRequiredCodeRule(var message: String) : RegionalCutoffOverrideRule {
    override fun check(item: RegionalCutoffOverride): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalCutoffOverrideRequiredLabelRule(var message: String) : RegionalCutoffOverrideRule {
    override fun check(item: RegionalCutoffOverride): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalCutoffOverrideOdometerRangeRule(var min: Int, var max: Int) : RegionalCutoffOverrideRule {
    override fun check(item: RegionalCutoffOverride): MutableList<String> {
        val value = item.odometer
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("odometer $value is below $min")
        }
        if (value > max) {
            problems.add("odometer $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a regional cutoff override. */
data class RegionalCutoffOverrideValidator(var rules: MutableList<RegionalCutoffOverrideRule>) {
    fun validate(item: RegionalCutoffOverride): MutableList<String> {
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

    fun requireValid(item: RegionalCutoffOverride) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalCutoffOverride>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalCutoffOverrideValidator(): RegionalCutoffOverrideValidator {
    return RegionalCutoffOverrideValidator(
        mutableListOf<RegionalCutoffOverrideRule>(
            RegionalCutoffOverrideRequiredCodeRule("regional cutoff override needs an entry"),
            RegionalCutoffOverrideRequiredLabelRule("regional cutoff override needs this field"),
            RegionalCutoffOverrideOdometerRangeRule(64, 1000),
        ),
    )
}
