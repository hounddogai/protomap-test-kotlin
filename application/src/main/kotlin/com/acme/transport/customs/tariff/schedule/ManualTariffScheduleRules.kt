package com.acme.transport.customs.tariff.schedule

/** A rule a manual tariff schedule must pass. Check returns its problems, or none. */
interface ManualTariffScheduleRule {
    fun check(item: ManualTariffSchedule): MutableList<String>
}

data class ManualTariffScheduleCodePrefixRule(var prefix: String) : ManualTariffScheduleRule {
    override fun check(item: ManualTariffSchedule): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ManualTariffScheduleCodeLengthRule(var maxLength: Int) : ManualTariffScheduleRule {
    override fun check(item: ManualTariffSchedule): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualTariffScheduleLaneLengthRule(var maxLength: Int) : ManualTariffScheduleRule {
    override fun check(item: ManualTariffSchedule): MutableList<String> {
        val size = item.lane.length
        if (size > maxLength) {
            return mutableListOf<String>("lane is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualTariffScheduleRequiredNameRule(var message: String) : ManualTariffScheduleRule {
    override fun check(item: ManualTariffSchedule): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual tariff schedule. */
data class ManualTariffScheduleValidator(var rules: MutableList<ManualTariffScheduleRule>) {
    fun validate(item: ManualTariffSchedule): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ManualTariffSchedule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualTariffSchedule>): MutableMap<String, MutableList<String>> {
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

fun defaultManualTariffScheduleValidator(): ManualTariffScheduleValidator {
    return ManualTariffScheduleValidator(
        mutableListOf<ManualTariffScheduleRule>(
            ManualTariffScheduleCodePrefixRule("X"),
            ManualTariffScheduleCodeLengthRule(3600),
            ManualTariffScheduleLaneLengthRule(86400),
            ManualTariffScheduleRequiredNameRule("manual tariff schedule needs an entry"),
        ),
    )
}
