package com.acme.transport.customs.hold.summary

/** A rule a hold summary must pass. Check returns its problems, or none. */
interface HoldSummaryRule {
    fun check(item: HoldSummary): MutableList<String>
}

data class HoldSummaryCodePrefixRule(var prefix: String) : HoldSummaryRule {
    override fun check(item: HoldSummary): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class HoldSummaryBudgetRangeRule(var min: Double, var max: Double) : HoldSummaryRule {
    override fun check(item: HoldSummary): MutableList<String> {
        val value = item.budget
        if (value < min || value > max) {
            return mutableListOf<String>("budget $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class HoldSummaryCodeLengthRule(var maxLength: Int) : HoldSummaryRule {
    override fun check(item: HoldSummary): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class HoldSummaryRequiredCodeRule(var message: String) : HoldSummaryRule {
    override fun check(item: HoldSummary): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a hold summary. */
data class HoldSummaryValidator(var rules: MutableList<HoldSummaryRule>) {
    fun validate(item: HoldSummary): MutableList<String> {
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

    fun requireValid(item: HoldSummary) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<HoldSummary>): MutableMap<String, MutableList<String>> {
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

fun defaultHoldSummaryValidator(): HoldSummaryValidator {
    return HoldSummaryValidator(
        mutableListOf<HoldSummaryRule>(
            HoldSummaryCodePrefixRule("OPS"),
            HoldSummaryBudgetRangeRule(3.5, 2780.7),
            HoldSummaryCodeLengthRule(3600),
            HoldSummaryRequiredCodeRule("hold summary needs this field"),
        ),
    )
}
