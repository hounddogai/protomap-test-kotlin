package com.acme.transport.safety.cameraevent.budget

/** A rule a regional camera event budget must pass. Check returns its problems, or none. */
interface RegionalCameraEventBudgetRule {
    fun check(item: RegionalCameraEventBudget): MutableList<String>
}

data class RegionalCameraEventBudgetNotesLengthRule(var maxLength: Int) : RegionalCameraEventBudgetRule {
    override fun check(item: RegionalCameraEventBudget): MutableList<String> {
        val size = item.notes.length
        if (size > maxLength) {
            return mutableListOf<String>("notes is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalCameraEventBudgetRequiredNotesRule(var message: String) : RegionalCameraEventBudgetRule {
    override fun check(item: RegionalCameraEventBudget): MutableList<String> {
        if (item.notes.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalCameraEventBudgetRequiredChannelRule(var message: String) : RegionalCameraEventBudgetRule {
    override fun check(item: RegionalCameraEventBudget): MutableList<String> {
        if (item.channel.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional camera event budget. */
data class RegionalCameraEventBudgetValidator(var rules: MutableList<RegionalCameraEventBudgetRule>) {
    fun validate(item: RegionalCameraEventBudget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RegionalCameraEventBudget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalCameraEventBudget>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalCameraEventBudgetValidator(): RegionalCameraEventBudgetValidator {
    return RegionalCameraEventBudgetValidator(
        mutableListOf<RegionalCameraEventBudgetRule>(
            RegionalCameraEventBudgetNotesLengthRule(120),
            RegionalCameraEventBudgetRequiredNotesRule("regional camera event budget needs a value"),
            RegionalCameraEventBudgetRequiredChannelRule("regional camera event budget needs an entry"),
        ),
    )
}
