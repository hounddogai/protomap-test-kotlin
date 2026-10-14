package com.acme.transport.customs.clearance.overrideitem

/** A rule a clearance schedule must pass. Check returns its problems, or none. */
interface ClearanceScheduleRule {
    fun check(item: ClearanceSchedule): MutableList<String>
}

data class ClearanceScheduleRequiredCodeRule(var message: String) : ClearanceScheduleRule {
    override fun check(item: ClearanceSchedule): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ClearanceScheduleRequiredNameRule(var message: String) : ClearanceScheduleRule {
    override fun check(item: ClearanceSchedule): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ClearanceScheduleRequiredDescriptionRule(var message: String) : ClearanceScheduleRule {
    override fun check(item: ClearanceSchedule): MutableList<String> {
        if (item.description.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ClearanceScheduleCodePrefixRule(var prefix: String) : ClearanceScheduleRule {
    override fun check(item: ClearanceSchedule): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ClearanceScheduleQuantityRangeRule(var min: Int, var max: Int) : ClearanceScheduleRule {
    override fun check(item: ClearanceSchedule): MutableList<String> {
        val value = item.quantity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("quantity $value is below $min")
        }
        if (value > max) {
            problems.add("quantity $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a clearance schedule. */
data class ClearanceScheduleValidator(var rules: MutableList<ClearanceScheduleRule>) {
    fun validate(item: ClearanceSchedule): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ClearanceSchedule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ClearanceSchedule>): MutableMap<String, MutableList<String>> {
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

fun defaultClearanceScheduleValidator(): ClearanceScheduleValidator {
    return ClearanceScheduleValidator(
        mutableListOf<ClearanceScheduleRule>(
            ClearanceScheduleRequiredCodeRule("clearance schedule needs an entry"),
            ClearanceScheduleRequiredNameRule("clearance schedule needs this field"),
            ClearanceScheduleRequiredDescriptionRule("clearance schedule needs a value"),
            ClearanceScheduleCodePrefixRule("REF"),
            ClearanceScheduleQuantityRangeRule(64, 86400),
        ),
    )
}
