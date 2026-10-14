package com.acme.transport.emissions.evcharge.plan

/** A rule a ev charge plan must pass. Check returns its problems, or none. */
interface EvChargePlanRule {
    fun check(item: EvChargePlan): MutableList<String>
}

data class EvChargePlanScoreRangeRule(var min: Double, var max: Double) : EvChargePlanRule {
    override fun check(item: EvChargePlan): MutableList<String> {
        val value = item.score
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("score $value is below $min")
        }
        if (value > max) {
            problems.add("score $value is above $max")
        }
        return problems
    }
}

data class EvChargePlanRevisionRangeRule(var min: Int, var max: Int) : EvChargePlanRule {
    override fun check(item: EvChargePlan): MutableList<String> {
        val value = item.revision
        if (value < min || value > max) {
            return mutableListOf<String>("revision $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class EvChargePlanAttemptsRangeRule(var min: Int, var max: Int) : EvChargePlanRule {
    override fun check(item: EvChargePlan): MutableList<String> {
        val value = item.attempts
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("attempts $value is below $min")
        }
        if (value > max) {
            problems.add("attempts $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a ev charge plan. */
data class EvChargePlanValidator(var rules: MutableList<EvChargePlanRule>) {
    fun validate(item: EvChargePlan): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: EvChargePlan) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<EvChargePlan>): MutableMap<String, MutableList<String>> {
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

fun defaultEvChargePlanValidator(): EvChargePlanValidator {
    return EvChargePlanValidator(
        mutableListOf<EvChargePlanRule>(
            EvChargePlanScoreRangeRule(2.2, 940.8),
            EvChargePlanRevisionRangeRule(3600, 86400),
            EvChargePlanAttemptsRangeRule(16, 120),
        ),
    )
}
