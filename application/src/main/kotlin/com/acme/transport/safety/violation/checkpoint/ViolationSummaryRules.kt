package com.acme.transport.safety.violation.checkpoint

/** A rule a violation summary must pass. Check returns its problems, or none. */
interface ViolationSummaryRule {
    fun check(item: ViolationSummary): MutableList<String>
}

data class ViolationSummaryEndsAtRangeRule(var min: Int, var max: Int) : ViolationSummaryRule {
    override fun check(item: ViolationSummary): MutableList<String> {
        val value = item.endsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ends at $value is below $min")
        }
        if (value > max) {
            problems.add("ends at $value is above $max")
        }
        return problems
    }
}

data class ViolationSummaryRequiredCategoryRule(var message: String) : ViolationSummaryRule {
    override fun check(item: ViolationSummary): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ViolationSummaryRequiredContactEmailRule(var message: String) : ViolationSummaryRule {
    override fun check(item: ViolationSummary): MutableList<String> {
        if (item.contactEmail.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ViolationSummaryCodePrefixRule(var prefix: String) : ViolationSummaryRule {
    override fun check(item: ViolationSummary): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a violation summary. */
data class ViolationSummaryValidator(var rules: MutableList<ViolationSummaryRule>) {
    fun validate(item: ViolationSummary): MutableList<String> {
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

    fun requireValid(item: ViolationSummary) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ViolationSummary>): MutableMap<String, MutableList<String>> {
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

fun defaultViolationSummaryValidator(): ViolationSummaryValidator {
    return ViolationSummaryValidator(
        mutableListOf<ViolationSummaryRule>(
            ViolationSummaryEndsAtRangeRule(80, 120),
            ViolationSummaryRequiredCategoryRule("violation summary needs a value"),
            ViolationSummaryRequiredContactEmailRule("violation summary needs a value"),
            ViolationSummaryCodePrefixRule("X"),
        ),
    )
}
