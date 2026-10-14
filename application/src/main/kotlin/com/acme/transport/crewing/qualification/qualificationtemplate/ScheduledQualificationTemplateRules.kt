package com.acme.transport.crewing.qualification.qualificationtemplate

import com.acme.transport.kit.text.normalize

/** A rule a scheduled qualification template must pass. Check returns its problems, or none. */
interface ScheduledQualificationTemplateRule {
    fun check(item: ScheduledQualificationTemplate): MutableList<String>
}

data class ScheduledQualificationTemplatePhoneLengthRule(
    var maxLength: Int,
) : ScheduledQualificationTemplateRule {
    override fun check(item: ScheduledQualificationTemplate): MutableList<String> {
        val size = item.phone.length
        if (size > maxLength) {
            return mutableListOf<String>("phone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledQualificationTemplateRequiredCodeRule(
    var message: String,
) : ScheduledQualificationTemplateRule {
    override fun check(item: ScheduledQualificationTemplate): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledQualificationTemplateAllowedPhoneRule(
    var allowed: MutableList<String>,
) : ScheduledQualificationTemplateRule {
    override fun check(item: ScheduledQualificationTemplate): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.phone))) {
            return mutableListOf<String>("phone ${item.phone} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled qualification template. */
data class ScheduledQualificationTemplateValidator(var rules: MutableList<ScheduledQualificationTemplateRule>) {
    fun validate(item: ScheduledQualificationTemplate): MutableList<String> {
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

    fun requireValid(item: ScheduledQualificationTemplate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledQualificationTemplate>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledQualificationTemplateValidator(): ScheduledQualificationTemplateValidator {
    return ScheduledQualificationTemplateValidator(
        mutableListOf<ScheduledQualificationTemplateRule>(
            ScheduledQualificationTemplatePhoneLengthRule(3600),
            ScheduledQualificationTemplateRequiredCodeRule("scheduled qualification template needs this field"),
            ScheduledQualificationTemplateAllowedPhoneRule(mutableListOf<String>("global", "seasonal")),
        ),
    )
}
