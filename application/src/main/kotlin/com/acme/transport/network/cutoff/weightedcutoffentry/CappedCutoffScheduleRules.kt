package com.acme.transport.network.cutoff.weightedcutoffentry

/** A rule a capped cutoff schedule must pass. Check returns its problems, or none. */
interface CappedCutoffScheduleRule {
    fun check(item: CappedCutoffSchedule): MutableList<String>
}

data class CappedCutoffScheduleMinimumRangeRule(var min: Double, var max: Double) : CappedCutoffScheduleRule {
    override fun check(item: CappedCutoffSchedule): MutableList<String> {
        val value = item.minimum
        if (value < min || value > max) {
            return mutableListOf<String>("minimum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CappedCutoffScheduleRequiredCodeRule(var message: String) : CappedCutoffScheduleRule {
    override fun check(item: CappedCutoffSchedule): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedCutoffScheduleNameLengthRule(var maxLength: Int) : CappedCutoffScheduleRule {
    override fun check(item: CappedCutoffSchedule): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped cutoff schedule. */
data class CappedCutoffScheduleValidator(var rules: MutableList<CappedCutoffScheduleRule>) {
    fun validate(item: CappedCutoffSchedule): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CappedCutoffSchedule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedCutoffSchedule>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedCutoffScheduleValidator(): CappedCutoffScheduleValidator {
    return CappedCutoffScheduleValidator(
        mutableListOf<CappedCutoffScheduleRule>(
            CappedCutoffScheduleMinimumRangeRule(8.9, 1375.0),
            CappedCutoffScheduleRequiredCodeRule("capped cutoff schedule needs this field"),
            CappedCutoffScheduleNameLengthRule(80),
        ),
    )
}
