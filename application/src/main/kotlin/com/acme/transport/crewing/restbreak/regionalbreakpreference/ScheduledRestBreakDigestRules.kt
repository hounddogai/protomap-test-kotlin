package com.acme.transport.crewing.restbreak.regionalbreakpreference

import com.acme.transport.kit.text.normalize

/** A rule a scheduled rest break digest must pass. Check returns its problems, or none. */
interface ScheduledRestBreakDigestRule {
    fun check(item: ScheduledRestBreakDigest): MutableList<String>
}

data class ScheduledRestBreakDigestAllowedCurrencyRule(
    var allowed: MutableList<String>,
) : ScheduledRestBreakDigestRule {
    override fun check(item: ScheduledRestBreakDigest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.currency))) {
            return mutableListOf<String>("currency ${item.currency} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledRestBreakDigestRequiredCarrierRule(var message: String) : ScheduledRestBreakDigestRule {
    override fun check(item: ScheduledRestBreakDigest): MutableList<String> {
        if (item.carrier.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledRestBreakDigestRequiredTitleRule(var message: String) : ScheduledRestBreakDigestRule {
    override fun check(item: ScheduledRestBreakDigest): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled rest break digest. */
data class ScheduledRestBreakDigestValidator(var rules: MutableList<ScheduledRestBreakDigestRule>) {
    fun validate(item: ScheduledRestBreakDigest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ScheduledRestBreakDigest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledRestBreakDigest>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledRestBreakDigestValidator(): ScheduledRestBreakDigestValidator {
    return ScheduledRestBreakDigestValidator(
        mutableListOf<ScheduledRestBreakDigestRule>(
            ScheduledRestBreakDigestAllowedCurrencyRule(mutableListOf<String>("regional", "manual")),
            ScheduledRestBreakDigestRequiredCarrierRule("scheduled rest break digest needs this field"),
            ScheduledRestBreakDigestRequiredTitleRule("scheduled rest break digest needs a value"),
        ),
    )
}
