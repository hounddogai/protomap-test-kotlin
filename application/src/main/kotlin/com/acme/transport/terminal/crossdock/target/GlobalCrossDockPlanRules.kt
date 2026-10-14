package com.acme.transport.terminal.crossdock.target

/** A rule a global cross dock plan must pass. Check returns its problems, or none. */
interface GlobalCrossDockPlanRule {
    fun check(item: GlobalCrossDockPlan): MutableList<String>
}

data class GlobalCrossDockPlanRequiredLabelRule(var message: String) : GlobalCrossDockPlanRule {
    override fun check(item: GlobalCrossDockPlan): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalCrossDockPlanLabelLengthRule(var maxLength: Int) : GlobalCrossDockPlanRule {
    override fun check(item: GlobalCrossDockPlan): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalCrossDockPlanWeightRangeRule(var min: Double, var max: Double) : GlobalCrossDockPlanRule {
    override fun check(item: GlobalCrossDockPlan): MutableList<String> {
        val value = item.weight
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("weight $value is below $min")
        }
        if (value > max) {
            problems.add("weight $value is above $max")
        }
        return problems
    }
}

data class GlobalCrossDockPlanRequiredCodeRule(var message: String) : GlobalCrossDockPlanRule {
    override fun check(item: GlobalCrossDockPlan): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalCrossDockPlanCodeLengthRule(var maxLength: Int) : GlobalCrossDockPlanRule {
    override fun check(item: GlobalCrossDockPlan): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global cross dock plan. */
data class GlobalCrossDockPlanValidator(var rules: MutableList<GlobalCrossDockPlanRule>) {
    fun validate(item: GlobalCrossDockPlan): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: GlobalCrossDockPlan) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalCrossDockPlan>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalCrossDockPlanValidator(): GlobalCrossDockPlanValidator {
    return GlobalCrossDockPlanValidator(
        mutableListOf<GlobalCrossDockPlanRule>(
            GlobalCrossDockPlanRequiredLabelRule("global cross dock plan needs a value"),
            GlobalCrossDockPlanLabelLengthRule(86400),
            GlobalCrossDockPlanWeightRangeRule(2.2, 2366.6),
            GlobalCrossDockPlanRequiredCodeRule("global cross dock plan needs a value"),
            GlobalCrossDockPlanCodeLengthRule(64),
        ),
    )
}
