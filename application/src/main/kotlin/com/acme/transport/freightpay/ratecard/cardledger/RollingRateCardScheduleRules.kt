package com.acme.transport.freightpay.ratecard.cardledger

import com.acme.transport.kit.text.normalize

/** A rule a rolling rate card schedule must pass. Check returns its problems, or none. */
interface RollingRateCardScheduleRule {
    fun check(item: RollingRateCardSchedule): MutableList<String>
}

data class RollingRateCardScheduleAllowedCodeRule(
    var allowed: MutableList<String>,
) : RollingRateCardScheduleRule {
    override fun check(item: RollingRateCardSchedule): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is too long")
        }
        return mutableListOf<String>()
    }
}

data class RollingRateCardScheduleRequiredCodeRule(var message: String) : RollingRateCardScheduleRule {
    override fun check(item: RollingRateCardSchedule): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingRateCardScheduleRequiredLocaleRule(var message: String) : RollingRateCardScheduleRule {
    override fun check(item: RollingRateCardSchedule): MutableList<String> {
        if (item.locale.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingRateCardScheduleCodePrefixRule(var prefix: String) : RollingRateCardScheduleRule {
    override fun check(item: RollingRateCardSchedule): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling rate card schedule. */
data class RollingRateCardScheduleValidator(var rules: MutableList<RollingRateCardScheduleRule>) {
    fun validate(item: RollingRateCardSchedule): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RollingRateCardSchedule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingRateCardSchedule>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingRateCardScheduleValidator(): RollingRateCardScheduleValidator {
    return RollingRateCardScheduleValidator(
        mutableListOf<RollingRateCardScheduleRule>(
            RollingRateCardScheduleAllowedCodeRule(mutableListOf<String>()),
            RollingRateCardScheduleRequiredCodeRule("rolling rate card schedule needs a value"),
            RollingRateCardScheduleRequiredLocaleRule("rolling rate card schedule needs a value"),
            RollingRateCardScheduleCodePrefixRule("OPS"),
        ),
    )
}
