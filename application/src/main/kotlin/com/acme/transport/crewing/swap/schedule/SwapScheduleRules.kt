package com.acme.transport.crewing.swap.schedule

/** A rule a swap schedule must pass. Check returns its problems, or none. */
interface SwapScheduleRule {
    fun check(item: SwapSchedule): MutableList<String>
}

data class SwapScheduleNameLengthRule(var maxLength: Int) : SwapScheduleRule {
    override fun check(item: SwapSchedule): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SwapScheduleCodeLengthRule(var maxLength: Int) : SwapScheduleRule {
    override fun check(item: SwapSchedule): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SwapScheduleRequiredCodeRule(var message: String) : SwapScheduleRule {
    override fun check(item: SwapSchedule): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a swap schedule. */
data class SwapScheduleValidator(var rules: MutableList<SwapScheduleRule>) {
    fun validate(item: SwapSchedule): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SwapSchedule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SwapSchedule>): MutableMap<String, MutableList<String>> {
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

fun defaultSwapScheduleValidator(): SwapScheduleValidator {
    return SwapScheduleValidator(
        mutableListOf<SwapScheduleRule>(
            SwapScheduleNameLengthRule(16),
            SwapScheduleCodeLengthRule(255),
            SwapScheduleRequiredCodeRule("swap schedule needs a value"),
        ),
    )
}
