package com.acme.transport.network.zone.threshold

/** A rule a scheduled zone template must pass. Check returns its problems, or none. */
interface ScheduledZoneTemplateRule {
    fun check(item: ScheduledZoneTemplate): MutableList<String>
}

data class ScheduledZoneTemplateRequiredSourceRule(var message: String) : ScheduledZoneTemplateRule {
    override fun check(item: ScheduledZoneTemplate): MutableList<String> {
        if (item.source.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledZoneTemplateWindowDaysRangeRule(var min: Int, var max: Int) : ScheduledZoneTemplateRule {
    override fun check(item: ScheduledZoneTemplate): MutableList<String> {
        val value = item.windowDays
        if (value < min || value > max) {
            return mutableListOf<String>("window days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledZoneTemplateAttemptsRangeRule(var min: Int, var max: Int) : ScheduledZoneTemplateRule {
    override fun check(item: ScheduledZoneTemplate): MutableList<String> {
        val value = item.attempts
        if (value < min || value > max) {
            return mutableListOf<String>("attempts $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledZoneTemplateCodePrefixRule(var prefix: String) : ScheduledZoneTemplateRule {
    override fun check(item: ScheduledZoneTemplate): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledZoneTemplateLabelLengthRule(var maxLength: Int) : ScheduledZoneTemplateRule {
    override fun check(item: ScheduledZoneTemplate): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled zone template. */
data class ScheduledZoneTemplateValidator(var rules: MutableList<ScheduledZoneTemplateRule>) {
    fun validate(item: ScheduledZoneTemplate): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ScheduledZoneTemplate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledZoneTemplate>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledZoneTemplateValidator(): ScheduledZoneTemplateValidator {
    return ScheduledZoneTemplateValidator(
        mutableListOf<ScheduledZoneTemplateRule>(
            ScheduledZoneTemplateRequiredSourceRule("scheduled zone template needs an entry"),
            ScheduledZoneTemplateWindowDaysRangeRule(16, 3600),
            ScheduledZoneTemplateAttemptsRangeRule(64, 120),
            ScheduledZoneTemplateCodePrefixRule("X"),
            ScheduledZoneTemplateLabelLengthRule(3600),
        ),
    )
}
