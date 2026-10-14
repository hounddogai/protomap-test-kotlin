package com.acme.transport.linehaul.backhaul.audit

import com.acme.transport.kit.text.normalize

/** A rule a strict backhaul audit must pass. Check returns its problems, or none. */
interface StrictBackhaulAuditRule {
    fun check(item: StrictBackhaulAudit): MutableList<String>
}

data class StrictBackhaulAuditCodeLengthRule(var maxLength: Int) : StrictBackhaulAuditRule {
    override fun check(item: StrictBackhaulAudit): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictBackhaulAuditAllowedTitleRule(var allowed: MutableList<String>) : StrictBackhaulAuditRule {
    override fun check(item: StrictBackhaulAudit): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class StrictBackhaulAuditTitleLengthRule(var maxLength: Int) : StrictBackhaulAuditRule {
    override fun check(item: StrictBackhaulAudit): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictBackhaulAuditPriorityRangeRule(var min: Int, var max: Int) : StrictBackhaulAuditRule {
    override fun check(item: StrictBackhaulAudit): MutableList<String> {
        val value = item.priority
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("priority $value is below $min")
        }
        if (value > max) {
            problems.add("priority $value is above $max")
        }
        return problems
    }
}

data class StrictBackhaulAuditCodePrefixRule(var prefix: String) : StrictBackhaulAuditRule {
    override fun check(item: StrictBackhaulAudit): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class StrictBackhaulAuditApprovedGuardRule(var limit: Int) : StrictBackhaulAuditRule {
    override fun check(item: StrictBackhaulAudit): MutableList<String> {
        if (item.approved && item.priority > limit) {
            return mutableListOf<String>("priority ${item.priority} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict backhaul audit. */
data class StrictBackhaulAuditValidator(var rules: MutableList<StrictBackhaulAuditRule>) {
    fun validate(item: StrictBackhaulAudit): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: StrictBackhaulAudit) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictBackhaulAudit>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictBackhaulAuditValidator(): StrictBackhaulAuditValidator {
    return StrictBackhaulAuditValidator(
        mutableListOf<StrictBackhaulAuditRule>(
            StrictBackhaulAuditCodeLengthRule(86400),
            StrictBackhaulAuditAllowedTitleRule(mutableListOf<String>("legacy", "seasonal")),
            StrictBackhaulAuditTitleLengthRule(1000),
            StrictBackhaulAuditPriorityRangeRule(1000, 86400),
            StrictBackhaulAuditCodePrefixRule("OPS"),
            StrictBackhaulAuditApprovedGuardRule(3600),
        ),
    )
}
