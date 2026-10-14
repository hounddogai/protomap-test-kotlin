package com.acme.transport.freightpay.audit.auditrule

import com.acme.transport.kit.text.normalize

/** A rule a adaptive audit rule must pass. Check returns its problems, or none. */
interface AdaptiveAuditRuleRule {
    fun check(item: AdaptiveAuditRule): MutableList<String>
}

data class AdaptiveAuditRuleRequiredNameRule(var message: String) : AdaptiveAuditRuleRule {
    override fun check(item: AdaptiveAuditRule): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveAuditRuleMinimumRangeRule(var min: Double, var max: Double) : AdaptiveAuditRuleRule {
    override fun check(item: AdaptiveAuditRule): MutableList<String> {
        val value = item.minimum
        if (value < min || value > max) {
            return mutableListOf<String>("minimum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveAuditRuleChannelLengthRule(var maxLength: Int) : AdaptiveAuditRuleRule {
    override fun check(item: AdaptiveAuditRule): MutableList<String> {
        val size = item.channel.length
        if (size > maxLength) {
            return mutableListOf<String>("channel is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveAuditRuleCodeLengthRule(var maxLength: Int) : AdaptiveAuditRuleRule {
    override fun check(item: AdaptiveAuditRule): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveAuditRuleAllowedNameRule(var allowed: MutableList<String>) : AdaptiveAuditRuleRule {
    override fun check(item: AdaptiveAuditRule): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is above maximum")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive audit rule. */
data class AdaptiveAuditRuleValidator(var rules: MutableList<AdaptiveAuditRuleRule>) {
    fun validate(item: AdaptiveAuditRule): MutableList<String> {
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

    fun requireValid(item: AdaptiveAuditRule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveAuditRule>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveAuditRuleValidator(): AdaptiveAuditRuleValidator {
    return AdaptiveAuditRuleValidator(
        mutableListOf<AdaptiveAuditRuleRule>(
            AdaptiveAuditRuleRequiredNameRule("adaptive audit rule needs a value"),
            AdaptiveAuditRuleMinimumRangeRule(4.6, 744.5),
            AdaptiveAuditRuleChannelLengthRule(80),
            AdaptiveAuditRuleCodeLengthRule(32),
            AdaptiveAuditRuleAllowedNameRule(mutableListOf<String>("global", "primary")),
        ),
    )
}
