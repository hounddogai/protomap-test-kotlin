package com.acme.transport.crewing.dutyperiod.bundle

/** A rule a duty period override must pass. Check returns its problems, or none. */
interface DutyPeriodOverrideRule {
    fun check(item: DutyPeriodOverride): MutableList<String>
}

data class DutyPeriodOverrideCodePrefixRule(var prefix: String) : DutyPeriodOverrideRule {
    override fun check(item: DutyPeriodOverride): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DutyPeriodOverrideRequiredCurrencyRule(var message: String) : DutyPeriodOverrideRule {
    override fun check(item: DutyPeriodOverride): MutableList<String> {
        if (item.currency.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DutyPeriodOverrideLabelLengthRule(var maxLength: Int) : DutyPeriodOverrideRule {
    override fun check(item: DutyPeriodOverride): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DutyPeriodOverrideThresholdRangeRule(var min: Double, var max: Double) : DutyPeriodOverrideRule {
    override fun check(item: DutyPeriodOverride): MutableList<String> {
        val value = item.threshold
        if (value < min || value > max) {
            return mutableListOf<String>("threshold $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a duty period override. */
data class DutyPeriodOverrideValidator(var rules: MutableList<DutyPeriodOverrideRule>) {
    fun validate(item: DutyPeriodOverride): MutableList<String> {
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

    fun requireValid(item: DutyPeriodOverride) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DutyPeriodOverride>): MutableMap<String, MutableList<String>> {
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

fun defaultDutyPeriodOverrideValidator(): DutyPeriodOverrideValidator {
    return DutyPeriodOverrideValidator(
        mutableListOf<DutyPeriodOverrideRule>(
            DutyPeriodOverrideCodePrefixRule("REF"),
            DutyPeriodOverrideRequiredCurrencyRule("duty period override needs an entry"),
            DutyPeriodOverrideLabelLengthRule(3600),
            DutyPeriodOverrideThresholdRangeRule(5.2, 4017.3),
        ),
    )
}
