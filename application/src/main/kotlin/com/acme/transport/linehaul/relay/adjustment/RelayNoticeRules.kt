package com.acme.transport.linehaul.relay.adjustment

import com.acme.transport.kit.text.normalize

/** A rule a relay notice must pass. Check returns its problems, or none. */
interface RelayNoticeRule {
    fun check(item: RelayNotice): MutableList<String>
}

data class RelayNoticeLocaleLengthRule(var maxLength: Int) : RelayNoticeRule {
    override fun check(item: RelayNotice): MutableList<String> {
        val size = item.locale.length
        if (size > maxLength) {
            return mutableListOf<String>("locale is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RelayNoticeAllowedTimezoneRule(var allowed: MutableList<String>) : RelayNoticeRule {
    override fun check(item: RelayNotice): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.timezone))) {
            return mutableListOf<String>("timezone ${item.timezone} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class RelayNoticeRequiredLocaleRule(var message: String) : RelayNoticeRule {
    override fun check(item: RelayNotice): MutableList<String> {
        if (item.locale.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a relay notice. */
data class RelayNoticeValidator(var rules: MutableList<RelayNoticeRule>) {
    fun validate(item: RelayNotice): MutableList<String> {
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

    fun requireValid(item: RelayNotice) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RelayNotice>): MutableMap<String, MutableList<String>> {
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

fun defaultRelayNoticeValidator(): RelayNoticeValidator {
    return RelayNoticeValidator(
        mutableListOf<RelayNoticeRule>(
            RelayNoticeLocaleLengthRule(1000),
            RelayNoticeAllowedTimezoneRule(mutableListOf<String>()),
            RelayNoticeRequiredLocaleRule("relay notice needs an entry"),
        ),
    )
}
