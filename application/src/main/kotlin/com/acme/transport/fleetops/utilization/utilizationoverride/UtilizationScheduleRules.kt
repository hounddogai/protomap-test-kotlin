package com.acme.transport.fleetops.utilization.utilizationoverride

/** A rule a utilization schedule must pass. Check returns its problems, or none. */
interface UtilizationScheduleRule {
    fun check(item: UtilizationSchedule): MutableList<String>
}

data class UtilizationScheduleCodeLengthRule(var maxLength: Int) : UtilizationScheduleRule {
    override fun check(item: UtilizationSchedule): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class UtilizationScheduleNameLengthRule(var maxLength: Int) : UtilizationScheduleRule {
    override fun check(item: UtilizationSchedule): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class UtilizationScheduleRequiredCarrierRule(var message: String) : UtilizationScheduleRule {
    override fun check(item: UtilizationSchedule): MutableList<String> {
        if (item.carrier.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a utilization schedule. */
data class UtilizationScheduleValidator(var rules: MutableList<UtilizationScheduleRule>) {
    fun validate(item: UtilizationSchedule): MutableList<String> {
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

    fun requireValid(item: UtilizationSchedule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<UtilizationSchedule>): MutableMap<String, MutableList<String>> {
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

fun defaultUtilizationScheduleValidator(): UtilizationScheduleValidator {
    return UtilizationScheduleValidator(
        mutableListOf<UtilizationScheduleRule>(
            UtilizationScheduleCodeLengthRule(16),
            UtilizationScheduleNameLengthRule(80),
            UtilizationScheduleRequiredCarrierRule("utilization schedule needs a value"),
        ),
    )
}
