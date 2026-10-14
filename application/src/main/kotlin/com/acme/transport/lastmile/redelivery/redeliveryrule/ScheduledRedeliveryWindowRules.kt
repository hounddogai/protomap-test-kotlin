package com.acme.transport.lastmile.redelivery.redeliveryrule

import com.acme.transport.kit.text.normalize

/** A rule a scheduled redelivery window must pass. Check returns its problems, or none. */
interface ScheduledRedeliveryWindowRule {
    fun check(item: ScheduledRedeliveryWindow): MutableList<String>
}

data class ScheduledRedeliveryWindowLabelLengthRule(var maxLength: Int) : ScheduledRedeliveryWindowRule {
    override fun check(item: ScheduledRedeliveryWindow): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledRedeliveryWindowAllowedNameRule(
    var allowed: MutableList<String>,
) : ScheduledRedeliveryWindowRule {
    override fun check(item: ScheduledRedeliveryWindow): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledRedeliveryWindowNameLengthRule(var maxLength: Int) : ScheduledRedeliveryWindowRule {
    override fun check(item: ScheduledRedeliveryWindow): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled redelivery window. */
data class ScheduledRedeliveryWindowValidator(var rules: MutableList<ScheduledRedeliveryWindowRule>) {
    fun validate(item: ScheduledRedeliveryWindow): MutableList<String> {
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

    fun requireValid(item: ScheduledRedeliveryWindow) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledRedeliveryWindow>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledRedeliveryWindowValidator(): ScheduledRedeliveryWindowValidator {
    return ScheduledRedeliveryWindowValidator(
        mutableListOf<ScheduledRedeliveryWindowRule>(
            ScheduledRedeliveryWindowLabelLengthRule(86400),
            ScheduledRedeliveryWindowAllowedNameRule(mutableListOf<String>("weighted", "global")),
            ScheduledRedeliveryWindowNameLengthRule(16),
        ),
    )
}
