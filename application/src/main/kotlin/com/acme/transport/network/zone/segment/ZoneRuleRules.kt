package com.acme.transport.network.zone.segment

import com.acme.transport.kit.text.normalize

/** A rule a zone rule must pass. Check returns its problems, or none. */
interface ZoneRuleRule {
    fun check(item: ZoneRule): MutableList<String>
}

data class ZoneRuleCodePrefixRule(var prefix: String) : ZoneRuleRule {
    override fun check(item: ZoneRule): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ZoneRuleEtaMinutesRangeRule(var min: Int, var max: Int) : ZoneRuleRule {
    override fun check(item: ZoneRule): MutableList<String> {
        val value = item.etaMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("eta minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ZoneRuleAllowedCodeRule(var allowed: MutableList<String>) : ZoneRuleRule {
    override fun check(item: ZoneRule): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class ZoneRuleCountryLengthRule(var maxLength: Int) : ZoneRuleRule {
    override fun check(item: ZoneRule): MutableList<String> {
        val size = item.country.length
        if (size > maxLength) {
            return mutableListOf<String>("country is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ZoneRulePriorityRangeRule(var min: Int, var max: Int) : ZoneRuleRule {
    override fun check(item: ZoneRule): MutableList<String> {
        val value = item.priority
        if (value < min || value > max) {
            return mutableListOf<String>("priority $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ZoneRuleWindowDaysRangeRule(var min: Int, var max: Int) : ZoneRuleRule {
    override fun check(item: ZoneRule): MutableList<String> {
        val value = item.windowDays
        if (value < min || value > max) {
            return mutableListOf<String>("window days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a zone rule. */
data class ZoneRuleValidator(var rules: MutableList<ZoneRuleRule>) {
    fun validate(item: ZoneRule): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ZoneRule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ZoneRule>): MutableMap<String, MutableList<String>> {
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

fun defaultZoneRuleValidator(): ZoneRuleValidator {
    return ZoneRuleValidator(
        mutableListOf<ZoneRuleRule>(
            ZoneRuleCodePrefixRule("ACM"),
            ZoneRuleEtaMinutesRangeRule(120, 255),
            ZoneRuleAllowedCodeRule(mutableListOf<String>("tiered", "legacy", "weighted")),
            ZoneRuleCountryLengthRule(64),
            ZoneRulePriorityRangeRule(64, 86400),
            ZoneRuleWindowDaysRangeRule(255, 3600),
        ),
    )
}
