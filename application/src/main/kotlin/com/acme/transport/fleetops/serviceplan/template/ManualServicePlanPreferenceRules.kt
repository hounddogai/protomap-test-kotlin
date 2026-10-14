package com.acme.transport.fleetops.serviceplan.template

/** A rule a manual service plan preference must pass. Check returns its problems, or none. */
interface ManualServicePlanPreferenceRule {
    fun check(item: ManualServicePlanPreference): MutableList<String>
}

data class ManualServicePlanPreferenceChannelLengthRule(var maxLength: Int) : ManualServicePlanPreferenceRule {
    override fun check(item: ManualServicePlanPreference): MutableList<String> {
        val size = item.channel.length
        if (size > maxLength) {
            return mutableListOf<String>("channel is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualServicePlanPreferenceUnitLengthRule(var maxLength: Int) : ManualServicePlanPreferenceRule {
    override fun check(item: ManualServicePlanPreference): MutableList<String> {
        val size = item.unit.length
        if (size > maxLength) {
            return mutableListOf<String>("unit is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualServicePlanPreferenceMarginRangeRule(
    var min: Double,
    var max: Double,
) : ManualServicePlanPreferenceRule {
    override fun check(item: ManualServicePlanPreference): MutableList<String> {
        val value = item.margin
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("margin $value is below $min")
        }
        if (value > max) {
            problems.add("margin $value is above $max")
        }
        return problems
    }
}

data class ManualServicePlanPreferenceRequiredUnitRule(var message: String) : ManualServicePlanPreferenceRule {
    override fun check(item: ManualServicePlanPreference): MutableList<String> {
        if (item.unit.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualServicePlanPreferenceRequiredChannelRule(
    var message: String,
) : ManualServicePlanPreferenceRule {
    override fun check(item: ManualServicePlanPreference): MutableList<String> {
        if (item.channel.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualServicePlanPreferenceRequiredTitleRule(var message: String) : ManualServicePlanPreferenceRule {
    override fun check(item: ManualServicePlanPreference): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual service plan preference. */
data class ManualServicePlanPreferenceValidator(var rules: MutableList<ManualServicePlanPreferenceRule>) {
    fun validate(item: ManualServicePlanPreference): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ManualServicePlanPreference) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualServicePlanPreference>): MutableMap<String, MutableList<String>> {
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

fun defaultManualServicePlanPreferenceValidator(): ManualServicePlanPreferenceValidator {
    return ManualServicePlanPreferenceValidator(
        mutableListOf<ManualServicePlanPreferenceRule>(
            ManualServicePlanPreferenceChannelLengthRule(120),
            ManualServicePlanPreferenceUnitLengthRule(120),
            ManualServicePlanPreferenceMarginRangeRule(0.3, 2064.7),
            ManualServicePlanPreferenceRequiredUnitRule("manual service plan preference needs an entry"),
            ManualServicePlanPreferenceRequiredChannelRule("manual service plan preference needs an entry"),
            ManualServicePlanPreferenceRequiredTitleRule("manual service plan preference needs a value"),
        ),
    )
}
