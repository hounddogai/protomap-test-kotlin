package com.acme.transport.customs.tariff.digest

/** A rule a tariff schedule must pass. Check returns its problems, or none. */
interface TariffScheduleRule {
    fun check(item: TariffSchedule): MutableList<String>
}

data class TariffScheduleCodePrefixRule(var prefix: String) : TariffScheduleRule {
    override fun check(item: TariffSchedule): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TariffScheduleRequiredCodeRule(var message: String) : TariffScheduleRule {
    override fun check(item: TariffSchedule): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TariffScheduleRequiredTitleRule(var message: String) : TariffScheduleRule {
    override fun check(item: TariffSchedule): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tariff schedule. */
data class TariffScheduleValidator(var rules: MutableList<TariffScheduleRule>) {
    fun validate(item: TariffSchedule): MutableList<String> {
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

    fun requireValid(item: TariffSchedule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TariffSchedule>): MutableMap<String, MutableList<String>> {
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

fun defaultTariffScheduleValidator(): TariffScheduleValidator {
    return TariffScheduleValidator(
        mutableListOf<TariffScheduleRule>(
            TariffScheduleCodePrefixRule("ACM"),
            TariffScheduleRequiredCodeRule("tariff schedule needs a value"),
            TariffScheduleRequiredTitleRule("tariff schedule needs this field"),
        ),
    )
}
