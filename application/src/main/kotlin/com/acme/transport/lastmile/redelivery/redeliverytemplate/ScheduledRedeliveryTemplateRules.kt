package com.acme.transport.lastmile.redelivery.redeliverytemplate

import com.acme.transport.kit.text.normalize

/** A rule a scheduled redelivery template must pass. Check returns its problems, or none. */
interface ScheduledRedeliveryTemplateRule {
    fun check(item: ScheduledRedeliveryTemplate): MutableList<String>
}

data class ScheduledRedeliveryTemplateSourceLengthRule(var maxLength: Int) : ScheduledRedeliveryTemplateRule {
    override fun check(item: ScheduledRedeliveryTemplate): MutableList<String> {
        val size = item.source.length
        if (size > maxLength) {
            return mutableListOf<String>("source is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledRedeliveryTemplateAllowedSourceRule(
    var allowed: MutableList<String>,
) : ScheduledRedeliveryTemplateRule {
    override fun check(item: ScheduledRedeliveryTemplate): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.source))) {
            return mutableListOf<String>("source ${item.source} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledRedeliveryTemplateRequiredSourceRule(
    var message: String,
) : ScheduledRedeliveryTemplateRule {
    override fun check(item: ScheduledRedeliveryTemplate): MutableList<String> {
        if (item.source.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledRedeliveryTemplateCodePrefixRule(var prefix: String) : ScheduledRedeliveryTemplateRule {
    override fun check(item: ScheduledRedeliveryTemplate): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledRedeliveryTemplateCodeLengthRule(var maxLength: Int) : ScheduledRedeliveryTemplateRule {
    override fun check(item: ScheduledRedeliveryTemplate): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledRedeliveryTemplateAttemptsRangeRule(
    var min: Int,
    var max: Int,
) : ScheduledRedeliveryTemplateRule {
    override fun check(item: ScheduledRedeliveryTemplate): MutableList<String> {
        val value = item.attempts
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("attempts $value is below $min")
        }
        if (value > max) {
            problems.add("attempts $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a scheduled redelivery template. */
data class ScheduledRedeliveryTemplateValidator(var rules: MutableList<ScheduledRedeliveryTemplateRule>) {
    fun validate(item: ScheduledRedeliveryTemplate): MutableList<String> {
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

    fun requireValid(item: ScheduledRedeliveryTemplate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledRedeliveryTemplate>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledRedeliveryTemplateValidator(): ScheduledRedeliveryTemplateValidator {
    return ScheduledRedeliveryTemplateValidator(
        mutableListOf<ScheduledRedeliveryTemplateRule>(
            ScheduledRedeliveryTemplateSourceLengthRule(80),
            ScheduledRedeliveryTemplateAllowedSourceRule(mutableListOf<String>("weighted", "capped")),
            ScheduledRedeliveryTemplateRequiredSourceRule("scheduled redelivery template needs a value"),
            ScheduledRedeliveryTemplateCodePrefixRule("X"),
            ScheduledRedeliveryTemplateCodeLengthRule(120),
            ScheduledRedeliveryTemplateAttemptsRangeRule(64, 1000),
        ),
    )
}
