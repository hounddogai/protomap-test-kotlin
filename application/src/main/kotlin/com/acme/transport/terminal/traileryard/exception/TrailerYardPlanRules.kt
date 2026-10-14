package com.acme.transport.terminal.traileryard.exception

/** A rule a trailer yard plan must pass. Check returns its problems, or none. */
interface TrailerYardPlanRule {
    fun check(item: TrailerYardPlan): MutableList<String>
}

data class TrailerYardPlanCodePrefixRule(var prefix: String) : TrailerYardPlanRule {
    override fun check(item: TrailerYardPlan): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TrailerYardPlanRequiredDockRule(var message: String) : TrailerYardPlanRule {
    override fun check(item: TrailerYardPlan): MutableList<String> {
        if (item.dock.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TrailerYardPlanLabelLengthRule(var maxLength: Int) : TrailerYardPlanRule {
    override fun check(item: TrailerYardPlan): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a trailer yard plan. */
data class TrailerYardPlanValidator(var rules: MutableList<TrailerYardPlanRule>) {
    fun validate(item: TrailerYardPlan): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TrailerYardPlan) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TrailerYardPlan>): MutableMap<String, MutableList<String>> {
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

fun defaultTrailerYardPlanValidator(): TrailerYardPlanValidator {
    return TrailerYardPlanValidator(
        mutableListOf<TrailerYardPlanRule>(
            TrailerYardPlanCodePrefixRule("X"),
            TrailerYardPlanRequiredDockRule("trailer yard plan needs a value"),
            TrailerYardPlanLabelLengthRule(255),
        ),
    )
}
