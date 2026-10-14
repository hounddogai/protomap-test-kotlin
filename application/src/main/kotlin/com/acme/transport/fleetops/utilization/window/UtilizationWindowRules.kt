package com.acme.transport.fleetops.utilization.window

/** A rule a utilization window must pass. Check returns its problems, or none. */
interface UtilizationWindowRule {
    fun check(item: UtilizationWindow): MutableList<String>
}

data class UtilizationWindowUpdatedAtRangeRule(var min: Int, var max: Int) : UtilizationWindowRule {
    override fun check(item: UtilizationWindow): MutableList<String> {
        val value = item.updatedAt
        if (value < min || value > max) {
            return mutableListOf<String>("updated at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class UtilizationWindowRequiredCodeRule(var message: String) : UtilizationWindowRule {
    override fun check(item: UtilizationWindow): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class UtilizationWindowAttemptsRangeRule(var min: Int, var max: Int) : UtilizationWindowRule {
    override fun check(item: UtilizationWindow): MutableList<String> {
        val value = item.attempts
        if (value < min || value > max) {
            return mutableListOf<String>("attempts $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a utilization window. */
data class UtilizationWindowValidator(var rules: MutableList<UtilizationWindowRule>) {
    fun validate(item: UtilizationWindow): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: UtilizationWindow) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<UtilizationWindow>): MutableMap<String, MutableList<String>> {
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

fun defaultUtilizationWindowValidator(): UtilizationWindowValidator {
    return UtilizationWindowValidator(
        mutableListOf<UtilizationWindowRule>(
            UtilizationWindowUpdatedAtRangeRule(86400, 86400),
            UtilizationWindowRequiredCodeRule("utilization window needs this field"),
            UtilizationWindowAttemptsRangeRule(64, 80),
        ),
    )
}
