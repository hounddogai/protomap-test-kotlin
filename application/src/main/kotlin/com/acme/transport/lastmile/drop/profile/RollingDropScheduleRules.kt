package com.acme.transport.lastmile.drop.profile

/** A rule a rolling drop schedule must pass. Check returns its problems, or none. */
interface RollingDropScheduleRule {
    fun check(item: RollingDropSchedule): MutableList<String>
}

data class RollingDropScheduleCodeLengthRule(var maxLength: Int) : RollingDropScheduleRule {
    override fun check(item: RollingDropSchedule): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingDropScheduleCodePrefixRule(var prefix: String) : RollingDropScheduleRule {
    override fun check(item: RollingDropSchedule): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RollingDropScheduleShiftCodeLengthRule(var maxLength: Int) : RollingDropScheduleRule {
    override fun check(item: RollingDropSchedule): MutableList<String> {
        val size = item.shiftCode.length
        if (size > maxLength) {
            return mutableListOf<String>("shift code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling drop schedule. */
data class RollingDropScheduleValidator(var rules: MutableList<RollingDropScheduleRule>) {
    fun validate(item: RollingDropSchedule): MutableList<String> {
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

    fun requireValid(item: RollingDropSchedule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingDropSchedule>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingDropScheduleValidator(): RollingDropScheduleValidator {
    return RollingDropScheduleValidator(
        mutableListOf<RollingDropScheduleRule>(
            RollingDropScheduleCodeLengthRule(16),
            RollingDropScheduleCodePrefixRule("Q"),
            RollingDropScheduleShiftCodeLengthRule(1000),
        ),
    )
}
