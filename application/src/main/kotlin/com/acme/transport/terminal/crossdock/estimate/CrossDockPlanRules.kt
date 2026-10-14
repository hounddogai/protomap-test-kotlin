package com.acme.transport.terminal.crossdock.estimate

/** A rule a cross dock plan must pass. Check returns its problems, or none. */
interface CrossDockPlanRule {
    fun check(item: CrossDockPlan): MutableList<String>
}

data class CrossDockPlanUpdatedAtRangeRule(var min: Int, var max: Int) : CrossDockPlanRule {
    override fun check(item: CrossDockPlan): MutableList<String> {
        val value = item.updatedAt
        if (value < min || value > max) {
            return mutableListOf<String>("updated at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CrossDockPlanCodePrefixRule(var prefix: String) : CrossDockPlanRule {
    override fun check(item: CrossDockPlan): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class CrossDockPlanRequiredTitleRule(var message: String) : CrossDockPlanRule {
    override fun check(item: CrossDockPlan): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CrossDockPlanCodeLengthRule(var maxLength: Int) : CrossDockPlanRule {
    override fun check(item: CrossDockPlan): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CrossDockPlanRequiredDockRule(var message: String) : CrossDockPlanRule {
    override fun check(item: CrossDockPlan): MutableList<String> {
        if (item.dock.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CrossDockPlanRequiredCodeRule(var message: String) : CrossDockPlanRule {
    override fun check(item: CrossDockPlan): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a cross dock plan. */
data class CrossDockPlanValidator(var rules: MutableList<CrossDockPlanRule>) {
    fun validate(item: CrossDockPlan): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CrossDockPlan) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CrossDockPlan>): MutableMap<String, MutableList<String>> {
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

fun defaultCrossDockPlanValidator(): CrossDockPlanValidator {
    return CrossDockPlanValidator(
        mutableListOf<CrossDockPlanRule>(
            CrossDockPlanUpdatedAtRangeRule(16, 32),
            CrossDockPlanCodePrefixRule("Q"),
            CrossDockPlanRequiredTitleRule("cross dock plan needs this field"),
            CrossDockPlanCodeLengthRule(64),
            CrossDockPlanRequiredDockRule("cross dock plan needs this field"),
            CrossDockPlanRequiredCodeRule("cross dock plan needs an entry"),
        ),
    )
}
