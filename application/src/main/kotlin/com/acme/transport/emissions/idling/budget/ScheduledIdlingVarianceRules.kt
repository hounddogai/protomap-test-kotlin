package com.acme.transport.emissions.idling.budget

import com.acme.transport.kit.text.normalize

/** A rule a scheduled idling variance must pass. Check returns its problems, or none. */
interface ScheduledIdlingVarianceRule {
    fun check(item: ScheduledIdlingVariance): MutableList<String>
}

data class ScheduledIdlingVarianceCodeLengthRule(var maxLength: Int) : ScheduledIdlingVarianceRule {
    override fun check(item: ScheduledIdlingVariance): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledIdlingVarianceCodePrefixRule(var prefix: String) : ScheduledIdlingVarianceRule {
    override fun check(item: ScheduledIdlingVariance): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledIdlingVarianceTitleLengthRule(var maxLength: Int) : ScheduledIdlingVarianceRule {
    override fun check(item: ScheduledIdlingVariance): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledIdlingVarianceRequiredTitleRule(var message: String) : ScheduledIdlingVarianceRule {
    override fun check(item: ScheduledIdlingVariance): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledIdlingVarianceAllowedCodeRule(
    var allowed: MutableList<String>,
) : ScheduledIdlingVarianceRule {
    override fun check(item: ScheduledIdlingVariance): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is expired")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledIdlingVarianceMaximumRangeRule(
    var min: Double,
    var max: Double,
) : ScheduledIdlingVarianceRule {
    override fun check(item: ScheduledIdlingVariance): MutableList<String> {
        val value = item.maximum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("maximum $value is below $min")
        }
        if (value > max) {
            problems.add("maximum $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a scheduled idling variance. */
data class ScheduledIdlingVarianceValidator(var rules: MutableList<ScheduledIdlingVarianceRule>) {
    fun validate(item: ScheduledIdlingVariance): MutableList<String> {
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

    fun requireValid(item: ScheduledIdlingVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledIdlingVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledIdlingVarianceValidator(): ScheduledIdlingVarianceValidator {
    return ScheduledIdlingVarianceValidator(
        mutableListOf<ScheduledIdlingVarianceRule>(
            ScheduledIdlingVarianceCodeLengthRule(86400),
            ScheduledIdlingVarianceCodePrefixRule("ACM"),
            ScheduledIdlingVarianceTitleLengthRule(32),
            ScheduledIdlingVarianceRequiredTitleRule("scheduled idling variance needs a value"),
            ScheduledIdlingVarianceAllowedCodeRule(mutableListOf<String>("capped", "weighted")),
            ScheduledIdlingVarianceMaximumRangeRule(2.1, 1864.4),
        ),
    )
}
