package com.acme.transport.emissions.fuelburn.burnthreshold

/** A rule a global fuel burn schedule must pass. Check returns its problems, or none. */
interface GlobalFuelBurnScheduleRule {
    fun check(item: GlobalFuelBurnSchedule): MutableList<String>
}

data class GlobalFuelBurnScheduleRequiredCodeRule(var message: String) : GlobalFuelBurnScheduleRule {
    override fun check(item: GlobalFuelBurnSchedule): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalFuelBurnScheduleCodePrefixRule(var prefix: String) : GlobalFuelBurnScheduleRule {
    override fun check(item: GlobalFuelBurnSchedule): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class GlobalFuelBurnScheduleUpdatedAtRangeRule(var min: Int, var max: Int) : GlobalFuelBurnScheduleRule {
    override fun check(item: GlobalFuelBurnSchedule): MutableList<String> {
        val value = item.updatedAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("updated at $value is below $min")
        }
        if (value > max) {
            problems.add("updated at $value is above $max")
        }
        return problems
    }
}

data class GlobalFuelBurnScheduleRequiredTitleRule(var message: String) : GlobalFuelBurnScheduleRule {
    override fun check(item: GlobalFuelBurnSchedule): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalFuelBurnScheduleLoadKgRangeRule(
    var min: Double,
    var max: Double,
) : GlobalFuelBurnScheduleRule {
    override fun check(item: GlobalFuelBurnSchedule): MutableList<String> {
        val value = item.loadKg
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("load kg $value is below $min")
        }
        if (value > max) {
            problems.add("load kg $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a global fuel burn schedule. */
data class GlobalFuelBurnScheduleValidator(var rules: MutableList<GlobalFuelBurnScheduleRule>) {
    fun validate(item: GlobalFuelBurnSchedule): MutableList<String> {
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

    fun requireValid(item: GlobalFuelBurnSchedule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalFuelBurnSchedule>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalFuelBurnScheduleValidator(): GlobalFuelBurnScheduleValidator {
    return GlobalFuelBurnScheduleValidator(
        mutableListOf<GlobalFuelBurnScheduleRule>(
            GlobalFuelBurnScheduleRequiredCodeRule("global fuel burn schedule needs this field"),
            GlobalFuelBurnScheduleCodePrefixRule("OPS"),
            GlobalFuelBurnScheduleUpdatedAtRangeRule(32, 64),
            GlobalFuelBurnScheduleRequiredTitleRule("global fuel burn schedule needs an entry"),
            GlobalFuelBurnScheduleLoadKgRangeRule(6.3, 325.1),
        ),
    )
}
