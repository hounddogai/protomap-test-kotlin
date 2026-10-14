package com.acme.transport.customs.tariff.schedule

/** A rule a strict tariff policy must pass. Check returns its problems, or none. */
interface StrictTariffPolicyRule {
    fun check(item: StrictTariffPolicy): MutableList<String>
}

data class StrictTariffPolicyRequiredDescriptionRule(var message: String) : StrictTariffPolicyRule {
    override fun check(item: StrictTariffPolicy): MutableList<String> {
        if (item.description.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictTariffPolicyCodePrefixRule(var prefix: String) : StrictTariffPolicyRule {
    override fun check(item: StrictTariffPolicy): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class StrictTariffPolicyThresholdRangeRule(var min: Double, var max: Double) : StrictTariffPolicyRule {
    override fun check(item: StrictTariffPolicy): MutableList<String> {
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

data class StrictTariffPolicyRequiredLabelRule(var message: String) : StrictTariffPolicyRule {
    override fun check(item: StrictTariffPolicy): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictTariffPolicyDescriptionLengthRule(var maxLength: Int) : StrictTariffPolicyRule {
    override fun check(item: StrictTariffPolicy): MutableList<String> {
        val size = item.description.length
        if (size > maxLength) {
            return mutableListOf<String>("description is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictTariffPolicyRequiredCodeRule(var message: String) : StrictTariffPolicyRule {
    override fun check(item: StrictTariffPolicy): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict tariff policy. */
data class StrictTariffPolicyValidator(var rules: MutableList<StrictTariffPolicyRule>) {
    fun validate(item: StrictTariffPolicy): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: StrictTariffPolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictTariffPolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictTariffPolicyValidator(): StrictTariffPolicyValidator {
    return StrictTariffPolicyValidator(
        mutableListOf<StrictTariffPolicyRule>(
            StrictTariffPolicyRequiredDescriptionRule("strict tariff policy needs this field"),
            StrictTariffPolicyCodePrefixRule("OPS"),
            StrictTariffPolicyThresholdRangeRule(9.8, 3741.4),
            StrictTariffPolicyRequiredLabelRule("strict tariff policy needs a value"),
            StrictTariffPolicyDescriptionLengthRule(64),
            StrictTariffPolicyRequiredCodeRule("strict tariff policy needs this field"),
        ),
    )
}
