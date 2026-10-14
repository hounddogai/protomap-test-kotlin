package com.acme.transport.crewing.dutyperiod.snapshot

/** A rule a duty period profile must pass. Check returns its problems, or none. */
interface DutyPeriodProfileRule {
    fun check(item: DutyPeriodProfile): MutableList<String>
}

data class DutyPeriodProfileThresholdRangeRule(var min: Double, var max: Double) : DutyPeriodProfileRule {
    override fun check(item: DutyPeriodProfile): MutableList<String> {
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

data class DutyPeriodProfileRequiredCountryRule(var message: String) : DutyPeriodProfileRule {
    override fun check(item: DutyPeriodProfile): MutableList<String> {
        if (item.country.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DutyPeriodProfileQuantityRangeRule(var min: Int, var max: Int) : DutyPeriodProfileRule {
    override fun check(item: DutyPeriodProfile): MutableList<String> {
        val value = item.quantity
        if (value < min || value > max) {
            return mutableListOf<String>("quantity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DutyPeriodProfileOdometerRangeRule(var min: Int, var max: Int) : DutyPeriodProfileRule {
    override fun check(item: DutyPeriodProfile): MutableList<String> {
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

data class DutyPeriodProfileCodePrefixRule(var prefix: String) : DutyPeriodProfileRule {
    override fun check(item: DutyPeriodProfile): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DutyPeriodProfileRequiredCodeRule(var message: String) : DutyPeriodProfileRule {
    override fun check(item: DutyPeriodProfile): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a duty period profile. */
data class DutyPeriodProfileValidator(var rules: MutableList<DutyPeriodProfileRule>) {
    fun validate(item: DutyPeriodProfile): MutableList<String> {
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

    fun requireValid(item: DutyPeriodProfile) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DutyPeriodProfile>): MutableMap<String, MutableList<String>> {
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

fun defaultDutyPeriodProfileValidator(): DutyPeriodProfileValidator {
    return DutyPeriodProfileValidator(
        mutableListOf<DutyPeriodProfileRule>(
            DutyPeriodProfileThresholdRangeRule(1.4, 3217.1),
            DutyPeriodProfileRequiredCountryRule("duty period profile needs an entry"),
            DutyPeriodProfileQuantityRangeRule(16, 3600),
            DutyPeriodProfileOdometerRangeRule(120, 1000),
            DutyPeriodProfileCodePrefixRule("OPS"),
            DutyPeriodProfileRequiredCodeRule("duty period profile needs this field"),
        ),
    )
}
