package com.acme.transport.crewing.dutyperiod.agreement

import com.acme.transport.kit.text.normalize

/** A rule a duty period template must pass. Check returns its problems, or none. */
interface DutyPeriodTemplateRule {
    fun check(item: DutyPeriodTemplate): MutableList<String>
}

data class DutyPeriodTemplateRequiredCodeRule(var message: String) : DutyPeriodTemplateRule {
    override fun check(item: DutyPeriodTemplate): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DutyPeriodTemplateRequiredLabelRule(var message: String) : DutyPeriodTemplateRule {
    override fun check(item: DutyPeriodTemplate): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DutyPeriodTemplateDockLengthRule(var maxLength: Int) : DutyPeriodTemplateRule {
    override fun check(item: DutyPeriodTemplate): MutableList<String> {
        val size = item.dock.length
        if (size > maxLength) {
            return mutableListOf<String>("dock is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DutyPeriodTemplateLabelLengthRule(var maxLength: Int) : DutyPeriodTemplateRule {
    override fun check(item: DutyPeriodTemplate): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DutyPeriodTemplateAllowedDockRule(var allowed: MutableList<String>) : DutyPeriodTemplateRule {
    override fun check(item: DutyPeriodTemplate): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.dock))) {
            return mutableListOf<String>("dock ${item.dock} is missing")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a duty period template. */
data class DutyPeriodTemplateValidator(var rules: MutableList<DutyPeriodTemplateRule>) {
    fun validate(item: DutyPeriodTemplate): MutableList<String> {
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

    fun requireValid(item: DutyPeriodTemplate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DutyPeriodTemplate>): MutableMap<String, MutableList<String>> {
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

fun defaultDutyPeriodTemplateValidator(): DutyPeriodTemplateValidator {
    return DutyPeriodTemplateValidator(
        mutableListOf<DutyPeriodTemplateRule>(
            DutyPeriodTemplateRequiredCodeRule("duty period template needs this field"),
            DutyPeriodTemplateRequiredLabelRule("duty period template needs this field"),
            DutyPeriodTemplateDockLengthRule(3600),
            DutyPeriodTemplateLabelLengthRule(16),
            DutyPeriodTemplateAllowedDockRule(mutableListOf<String>("strict")),
        ),
    )
}
