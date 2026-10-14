package com.acme.transport.linehaul.laneplan.target

/** A rule a lane plan schedule must pass. Check returns its problems, or none. */
interface LanePlanScheduleRule {
    fun check(item: LanePlanSchedule): MutableList<String>
}

data class LanePlanScheduleCodeLengthRule(var maxLength: Int) : LanePlanScheduleRule {
    override fun check(item: LanePlanSchedule): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LanePlanScheduleRequiredVehicleIdRule(var message: String) : LanePlanScheduleRule {
    override fun check(item: LanePlanSchedule): MutableList<String> {
        if (item.vehicleId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LanePlanScheduleRequiredChannelRule(var message: String) : LanePlanScheduleRule {
    override fun check(item: LanePlanSchedule): MutableList<String> {
        if (item.channel.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LanePlanScheduleCodePrefixRule(var prefix: String) : LanePlanScheduleRule {
    override fun check(item: LanePlanSchedule): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LanePlanScheduleRequiredLabelRule(var message: String) : LanePlanScheduleRule {
    override fun check(item: LanePlanSchedule): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lane plan schedule. */
data class LanePlanScheduleValidator(var rules: MutableList<LanePlanScheduleRule>) {
    fun validate(item: LanePlanSchedule): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LanePlanSchedule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LanePlanSchedule>): MutableMap<String, MutableList<String>> {
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

fun defaultLanePlanScheduleValidator(): LanePlanScheduleValidator {
    return LanePlanScheduleValidator(
        mutableListOf<LanePlanScheduleRule>(
            LanePlanScheduleCodeLengthRule(3600),
            LanePlanScheduleRequiredVehicleIdRule("lane plan schedule needs this field"),
            LanePlanScheduleRequiredChannelRule("lane plan schedule needs a value"),
            LanePlanScheduleCodePrefixRule("ACM"),
            LanePlanScheduleRequiredLabelRule("lane plan schedule needs this field"),
        ),
    )
}
