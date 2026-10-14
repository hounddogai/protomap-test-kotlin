package com.acme.transport.safety.cameraevent.audit

/** A rule a camera event budget must pass. Check returns its problems, or none. */
interface CameraEventBudgetRule {
    fun check(item: CameraEventBudget): MutableList<String>
}

data class CameraEventBudgetGraceDaysRangeRule(var min: Int, var max: Int) : CameraEventBudgetRule {
    override fun check(item: CameraEventBudget): MutableList<String> {
        val value = item.graceDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("grace days $value is below $min")
        }
        if (value > max) {
            problems.add("grace days $value is above $max")
        }
        return problems
    }
}

data class CameraEventBudgetRequiredCarrierRule(var message: String) : CameraEventBudgetRule {
    override fun check(item: CameraEventBudget): MutableList<String> {
        if (item.carrier.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CameraEventBudgetRequiredLabelRule(var message: String) : CameraEventBudgetRule {
    override fun check(item: CameraEventBudget): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a camera event budget. */
data class CameraEventBudgetValidator(var rules: MutableList<CameraEventBudgetRule>) {
    fun validate(item: CameraEventBudget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CameraEventBudget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CameraEventBudget>): MutableMap<String, MutableList<String>> {
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

fun defaultCameraEventBudgetValidator(): CameraEventBudgetValidator {
    return CameraEventBudgetValidator(
        mutableListOf<CameraEventBudgetRule>(
            CameraEventBudgetGraceDaysRangeRule(120, 255),
            CameraEventBudgetRequiredCarrierRule("camera event budget needs this field"),
            CameraEventBudgetRequiredLabelRule("camera event budget needs an entry"),
        ),
    )
}
