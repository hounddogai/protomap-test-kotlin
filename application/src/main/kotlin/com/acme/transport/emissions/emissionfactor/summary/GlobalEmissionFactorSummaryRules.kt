package com.acme.transport.emissions.emissionfactor.summary

/** A rule a global emission factor summary must pass. Check returns its problems, or none. */
interface GlobalEmissionFactorSummaryRule {
    fun check(item: GlobalEmissionFactorSummary): MutableList<String>
}

data class GlobalEmissionFactorSummaryLabelLengthRule(var maxLength: Int) : GlobalEmissionFactorSummaryRule {
    override fun check(item: GlobalEmissionFactorSummary): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalEmissionFactorSummaryCodePrefixRule(var prefix: String) : GlobalEmissionFactorSummaryRule {
    override fun check(item: GlobalEmissionFactorSummary): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class GlobalEmissionFactorSummaryRevisionRangeRule(
    var min: Int,
    var max: Int,
) : GlobalEmissionFactorSummaryRule {
    override fun check(item: GlobalEmissionFactorSummary): MutableList<String> {
        val value = item.revision
        if (value < min || value > max) {
            return mutableListOf<String>("revision $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class GlobalEmissionFactorSummaryWindowDaysRangeRule(
    var min: Int,
    var max: Int,
) : GlobalEmissionFactorSummaryRule {
    override fun check(item: GlobalEmissionFactorSummary): MutableList<String> {
        val value = item.windowDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("window days $value is below $min")
        }
        if (value > max) {
            problems.add("window days $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a global emission factor summary. */
data class GlobalEmissionFactorSummaryValidator(var rules: MutableList<GlobalEmissionFactorSummaryRule>) {
    fun validate(item: GlobalEmissionFactorSummary): MutableList<String> {
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

    fun requireValid(item: GlobalEmissionFactorSummary) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalEmissionFactorSummary>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalEmissionFactorSummaryValidator(): GlobalEmissionFactorSummaryValidator {
    return GlobalEmissionFactorSummaryValidator(
        mutableListOf<GlobalEmissionFactorSummaryRule>(
            GlobalEmissionFactorSummaryLabelLengthRule(120),
            GlobalEmissionFactorSummaryCodePrefixRule("Q"),
            GlobalEmissionFactorSummaryRevisionRangeRule(64, 255),
            GlobalEmissionFactorSummaryWindowDaysRangeRule(120, 255),
        ),
    )
}
