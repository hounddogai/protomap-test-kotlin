package com.acme.transport.safety.nearmiss.batch

import com.acme.transport.kit.text.normalize

/** A rule a scheduled near miss plan must pass. Check returns its problems, or none. */
interface ScheduledNearMissPlanRule {
    fun check(item: ScheduledNearMissPlan): MutableList<String>
}

data class ScheduledNearMissPlanRequiredCodeRule(var message: String) : ScheduledNearMissPlanRule {
    override fun check(item: ScheduledNearMissPlan): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledNearMissPlanMarginRangeRule(var min: Double, var max: Double) : ScheduledNearMissPlanRule {
    override fun check(item: ScheduledNearMissPlan): MutableList<String> {
        val value = item.margin
        if (value < min || value > max) {
            return mutableListOf<String>("margin $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledNearMissPlanAllowedRouteCodeRule(
    var allowed: MutableList<String>,
) : ScheduledNearMissPlanRule {
    override fun check(item: ScheduledNearMissPlan): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.routeCode))) {
            return mutableListOf<String>("route code ${item.routeCode} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledNearMissPlanEtaMinutesRangeRule(var min: Int, var max: Int) : ScheduledNearMissPlanRule {
    override fun check(item: ScheduledNearMissPlan): MutableList<String> {
        val value = item.etaMinutes
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("eta minutes $value is below $min")
        }
        if (value > max) {
            problems.add("eta minutes $value is above $max")
        }
        return problems
    }
}

data class ScheduledNearMissPlanRouteCodeLengthRule(var maxLength: Int) : ScheduledNearMissPlanRule {
    override fun check(item: ScheduledNearMissPlan): MutableList<String> {
        val size = item.routeCode.length
        if (size > maxLength) {
            return mutableListOf<String>("route code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledNearMissPlanCodePrefixRule(var prefix: String) : ScheduledNearMissPlanRule {
    override fun check(item: ScheduledNearMissPlan): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled near miss plan. */
data class ScheduledNearMissPlanValidator(var rules: MutableList<ScheduledNearMissPlanRule>) {
    fun validate(item: ScheduledNearMissPlan): MutableList<String> {
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

    fun requireValid(item: ScheduledNearMissPlan) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledNearMissPlan>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledNearMissPlanValidator(): ScheduledNearMissPlanValidator {
    return ScheduledNearMissPlanValidator(
        mutableListOf<ScheduledNearMissPlanRule>(
            ScheduledNearMissPlanRequiredCodeRule("scheduled near miss plan needs this field"),
            ScheduledNearMissPlanMarginRangeRule(4.1, 834.0),
            ScheduledNearMissPlanAllowedRouteCodeRule(mutableListOf<String>("manual")),
            ScheduledNearMissPlanEtaMinutesRangeRule(255, 86400),
            ScheduledNearMissPlanRouteCodeLengthRule(86400),
            ScheduledNearMissPlanCodePrefixRule("X"),
        ),
    )
}
