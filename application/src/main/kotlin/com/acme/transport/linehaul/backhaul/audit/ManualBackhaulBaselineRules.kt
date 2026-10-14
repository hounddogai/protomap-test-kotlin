package com.acme.transport.linehaul.backhaul.audit

/** A rule a manual backhaul baseline must pass. Check returns its problems, or none. */
interface ManualBackhaulBaselineRule {
    fun check(item: ManualBackhaulBaseline): MutableList<String>
}

data class ManualBackhaulBaselineLabelLengthRule(var maxLength: Int) : ManualBackhaulBaselineRule {
    override fun check(item: ManualBackhaulBaseline): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualBackhaulBaselineRequiredCodeRule(var message: String) : ManualBackhaulBaselineRule {
    override fun check(item: ManualBackhaulBaseline): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualBackhaulBaselineAmountRangeRule(
    var min: Double,
    var max: Double,
) : ManualBackhaulBaselineRule {
    override fun check(item: ManualBackhaulBaseline): MutableList<String> {
        val value = item.amount
        if (value < min || value > max) {
            return mutableListOf<String>("amount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ManualBackhaulBaselineRequiredLabelRule(var message: String) : ManualBackhaulBaselineRule {
    override fun check(item: ManualBackhaulBaseline): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual backhaul baseline. */
data class ManualBackhaulBaselineValidator(var rules: MutableList<ManualBackhaulBaselineRule>) {
    fun validate(item: ManualBackhaulBaseline): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ManualBackhaulBaseline) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualBackhaulBaseline>): MutableMap<String, MutableList<String>> {
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

fun defaultManualBackhaulBaselineValidator(): ManualBackhaulBaselineValidator {
    return ManualBackhaulBaselineValidator(
        mutableListOf<ManualBackhaulBaselineRule>(
            ManualBackhaulBaselineLabelLengthRule(32),
            ManualBackhaulBaselineRequiredCodeRule("manual backhaul baseline needs an entry"),
            ManualBackhaulBaselineAmountRangeRule(4.2, 4459.9),
            ManualBackhaulBaselineRequiredLabelRule("manual backhaul baseline needs this field"),
        ),
    )
}
