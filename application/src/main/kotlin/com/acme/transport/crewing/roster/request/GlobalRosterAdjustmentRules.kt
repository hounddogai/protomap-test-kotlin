package com.acme.transport.crewing.roster.request

/** A rule a global roster adjustment must pass. Check returns its problems, or none. */
interface GlobalRosterAdjustmentRule {
    fun check(item: GlobalRosterAdjustment): MutableList<String>
}

data class GlobalRosterAdjustmentCodePrefixRule(var prefix: String) : GlobalRosterAdjustmentRule {
    override fun check(item: GlobalRosterAdjustment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class GlobalRosterAdjustmentRequiredLabelRule(var message: String) : GlobalRosterAdjustmentRule {
    override fun check(item: GlobalRosterAdjustment): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalRosterAdjustmentCodeLengthRule(var maxLength: Int) : GlobalRosterAdjustmentRule {
    override fun check(item: GlobalRosterAdjustment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalRosterAdjustmentRequiredCodeRule(var message: String) : GlobalRosterAdjustmentRule {
    override fun check(item: GlobalRosterAdjustment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalRosterAdjustmentRequiredTitleRule(var message: String) : GlobalRosterAdjustmentRule {
    override fun check(item: GlobalRosterAdjustment): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalRosterAdjustmentRetriesRangeRule(var min: Int, var max: Int) : GlobalRosterAdjustmentRule {
    override fun check(item: GlobalRosterAdjustment): MutableList<String> {
        val value = item.retries
        if (value < min || value > max) {
            return mutableListOf<String>("retries $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global roster adjustment. */
data class GlobalRosterAdjustmentValidator(var rules: MutableList<GlobalRosterAdjustmentRule>) {
    fun validate(item: GlobalRosterAdjustment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: GlobalRosterAdjustment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalRosterAdjustment>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalRosterAdjustmentValidator(): GlobalRosterAdjustmentValidator {
    return GlobalRosterAdjustmentValidator(
        mutableListOf<GlobalRosterAdjustmentRule>(
            GlobalRosterAdjustmentCodePrefixRule("Q"),
            GlobalRosterAdjustmentRequiredLabelRule("global roster adjustment needs a value"),
            GlobalRosterAdjustmentCodeLengthRule(80),
            GlobalRosterAdjustmentRequiredCodeRule("global roster adjustment needs this field"),
            GlobalRosterAdjustmentRequiredTitleRule("global roster adjustment needs a value"),
            GlobalRosterAdjustmentRetriesRangeRule(16, 16),
        ),
    )
}
