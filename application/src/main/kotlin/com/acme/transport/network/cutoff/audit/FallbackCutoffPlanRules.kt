package com.acme.transport.network.cutoff.audit

/** A rule a fallback cutoff plan must pass. Check returns its problems, or none. */
interface FallbackCutoffPlanRule {
    fun check(item: FallbackCutoffPlan): MutableList<String>
}

data class FallbackCutoffPlanNameLengthRule(var maxLength: Int) : FallbackCutoffPlanRule {
    override fun check(item: FallbackCutoffPlan): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackCutoffPlanRequiredNameRule(var message: String) : FallbackCutoffPlanRule {
    override fun check(item: FallbackCutoffPlan): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackCutoffPlanRequiredRouteCodeRule(var message: String) : FallbackCutoffPlanRule {
    override fun check(item: FallbackCutoffPlan): MutableList<String> {
        if (item.routeCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback cutoff plan. */
data class FallbackCutoffPlanValidator(var rules: MutableList<FallbackCutoffPlanRule>) {
    fun validate(item: FallbackCutoffPlan): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FallbackCutoffPlan) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackCutoffPlan>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackCutoffPlanValidator(): FallbackCutoffPlanValidator {
    return FallbackCutoffPlanValidator(
        mutableListOf<FallbackCutoffPlanRule>(
            FallbackCutoffPlanNameLengthRule(16),
            FallbackCutoffPlanRequiredNameRule("fallback cutoff plan needs a value"),
            FallbackCutoffPlanRequiredRouteCodeRule("fallback cutoff plan needs a value"),
        ),
    )
}
