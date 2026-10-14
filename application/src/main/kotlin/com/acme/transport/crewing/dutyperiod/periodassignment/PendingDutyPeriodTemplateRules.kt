package com.acme.transport.crewing.dutyperiod.periodassignment

import com.acme.transport.kit.text.normalize

/** A rule a pending duty period template must pass. Check returns its problems, or none. */
interface PendingDutyPeriodTemplateRule {
    fun check(item: PendingDutyPeriodTemplate): MutableList<String>
}

data class PendingDutyPeriodTemplateWindowDaysRangeRule(
    var min: Int,
    var max: Int,
) : PendingDutyPeriodTemplateRule {
    override fun check(item: PendingDutyPeriodTemplate): MutableList<String> {
        val value = item.windowDays
        if (value < min || value > max) {
            return mutableListOf<String>("window days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PendingDutyPeriodTemplateRequiredLabelRule(var message: String) : PendingDutyPeriodTemplateRule {
    override fun check(item: PendingDutyPeriodTemplate): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingDutyPeriodTemplateLabelLengthRule(var maxLength: Int) : PendingDutyPeriodTemplateRule {
    override fun check(item: PendingDutyPeriodTemplate): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingDutyPeriodTemplateAllowedLabelRule(
    var allowed: MutableList<String>,
) : PendingDutyPeriodTemplateRule {
    override fun check(item: PendingDutyPeriodTemplate): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class PendingDutyPeriodTemplateContactEmailLengthRule(var maxLength: Int) : PendingDutyPeriodTemplateRule {
    override fun check(item: PendingDutyPeriodTemplate): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending duty period template. */
data class PendingDutyPeriodTemplateValidator(var rules: MutableList<PendingDutyPeriodTemplateRule>) {
    fun validate(item: PendingDutyPeriodTemplate): MutableList<String> {
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

    fun requireValid(item: PendingDutyPeriodTemplate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingDutyPeriodTemplate>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingDutyPeriodTemplateValidator(): PendingDutyPeriodTemplateValidator {
    return PendingDutyPeriodTemplateValidator(
        mutableListOf<PendingDutyPeriodTemplateRule>(
            PendingDutyPeriodTemplateWindowDaysRangeRule(64, 255),
            PendingDutyPeriodTemplateRequiredLabelRule("pending duty period template needs an entry"),
            PendingDutyPeriodTemplateLabelLengthRule(86400),
            PendingDutyPeriodTemplateAllowedLabelRule(mutableListOf<String>()),
            PendingDutyPeriodTemplateContactEmailLengthRule(1000),
        ),
    )
}
