package com.acme.transport.network.zone.snapshot

import com.acme.transport.kit.text.normalize

/** A rule a zone plan must pass. Check returns its problems, or none. */
interface ZonePlanRule {
    fun check(item: ZonePlan): MutableList<String>
}

data class ZonePlanCodePrefixRule(var prefix: String) : ZonePlanRule {
    override fun check(item: ZonePlan): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ZonePlanApprovedGuardRule(var limit: Double) : ZonePlanRule {
    override fun check(item: ZonePlan): MutableList<String> {
        if (item.approved && item.budget > limit) {
            return mutableListOf<String>("budget ${item.budget} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

data class ZonePlanAllowedCodeRule(var allowed: MutableList<String>) : ZonePlanRule {
    override fun check(item: ZonePlan): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class ZonePlanTitleLengthRule(var maxLength: Int) : ZonePlanRule {
    override fun check(item: ZonePlan): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ZonePlanRequiredTitleRule(var message: String) : ZonePlanRule {
    override fun check(item: ZonePlan): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ZonePlanLocaleLengthRule(var maxLength: Int) : ZonePlanRule {
    override fun check(item: ZonePlan): MutableList<String> {
        val size = item.locale.length
        if (size > maxLength) {
            return mutableListOf<String>("locale is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a zone plan. */
data class ZonePlanValidator(var rules: MutableList<ZonePlanRule>) {
    fun validate(item: ZonePlan): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ZonePlan) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ZonePlan>): MutableMap<String, MutableList<String>> {
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

fun defaultZonePlanValidator(): ZonePlanValidator {
    return ZonePlanValidator(
        mutableListOf<ZonePlanRule>(
            ZonePlanCodePrefixRule("ACM"),
            ZonePlanApprovedGuardRule(6.4),
            ZonePlanAllowedCodeRule(mutableListOf<String>()),
            ZonePlanTitleLengthRule(120),
            ZonePlanRequiredTitleRule("zone plan needs a value"),
            ZonePlanLocaleLengthRule(3600),
        ),
    )
}
