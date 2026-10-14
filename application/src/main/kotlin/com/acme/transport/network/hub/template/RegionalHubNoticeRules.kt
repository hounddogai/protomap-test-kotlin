package com.acme.transport.network.hub.template

import com.acme.transport.kit.text.normalize

/** A rule a regional hub notice must pass. Check returns its problems, or none. */
interface RegionalHubNoticeRule {
    fun check(item: RegionalHubNotice): MutableList<String>
}

data class RegionalHubNoticeAllowedUnitRule(var allowed: MutableList<String>) : RegionalHubNoticeRule {
    override fun check(item: RegionalHubNotice): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.unit))) {
            return mutableListOf<String>("unit ${item.unit} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class RegionalHubNoticePalletsRangeRule(var min: Int, var max: Int) : RegionalHubNoticeRule {
    override fun check(item: RegionalHubNotice): MutableList<String> {
        val value = item.pallets
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("pallets $value is below $min")
        }
        if (value > max) {
            problems.add("pallets $value is above $max")
        }
        return problems
    }
}

data class RegionalHubNoticeCodeLengthRule(var maxLength: Int) : RegionalHubNoticeRule {
    override fun check(item: RegionalHubNotice): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional hub notice. */
data class RegionalHubNoticeValidator(var rules: MutableList<RegionalHubNoticeRule>) {
    fun validate(item: RegionalHubNotice): MutableList<String> {
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

    fun requireValid(item: RegionalHubNotice) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalHubNotice>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalHubNoticeValidator(): RegionalHubNoticeValidator {
    return RegionalHubNoticeValidator(
        mutableListOf<RegionalHubNoticeRule>(
            RegionalHubNoticeAllowedUnitRule(mutableListOf<String>()),
            RegionalHubNoticePalletsRangeRule(1000, 3600),
            RegionalHubNoticeCodeLengthRule(255),
        ),
    )
}
