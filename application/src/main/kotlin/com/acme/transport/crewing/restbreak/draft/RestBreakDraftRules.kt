package com.acme.transport.crewing.restbreak.draft

/** A rule a rest break draft must pass. Check returns its problems, or none. */
interface RestBreakDraftRule {
    fun check(item: RestBreakDraft): MutableList<String>
}

data class RestBreakDraftAttemptsRangeRule(var min: Int, var max: Int) : RestBreakDraftRule {
    override fun check(item: RestBreakDraft): MutableList<String> {
        val value = item.attempts
        if (value < min || value > max) {
            return mutableListOf<String>("attempts $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RestBreakDraftRequiredNameRule(var message: String) : RestBreakDraftRule {
    override fun check(item: RestBreakDraft): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RestBreakDraftWeightRangeRule(var min: Double, var max: Double) : RestBreakDraftRule {
    override fun check(item: RestBreakDraft): MutableList<String> {
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

/** Runs every rule of a rest break draft. */
data class RestBreakDraftValidator(var rules: MutableList<RestBreakDraftRule>) {
    fun validate(item: RestBreakDraft): MutableList<String> {
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

    fun requireValid(item: RestBreakDraft) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RestBreakDraft>): MutableMap<String, MutableList<String>> {
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

fun defaultRestBreakDraftValidator(): RestBreakDraftValidator {
    return RestBreakDraftValidator(
        mutableListOf<RestBreakDraftRule>(
            RestBreakDraftAttemptsRangeRule(120, 1000),
            RestBreakDraftRequiredNameRule("rest break draft needs this field"),
            RestBreakDraftWeightRangeRule(1.6, 4131.7),
        ),
    )
}
