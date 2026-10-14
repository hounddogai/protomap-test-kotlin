package com.acme.transport.safety.violation.violationslot

/** A rule a scheduled violation template must pass. Check returns its problems, or none. */
interface ScheduledViolationTemplateRule {
    fun check(item: ScheduledViolationTemplate): MutableList<String>
}

data class ScheduledViolationTemplateRequiredNameRule(var message: String) : ScheduledViolationTemplateRule {
    override fun check(item: ScheduledViolationTemplate): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledViolationTemplateArchivedGuardRule(var limit: Int) : ScheduledViolationTemplateRule {
    override fun check(item: ScheduledViolationTemplate): MutableList<String> {
        if (item.archived && item.batchSize > limit) {
            return mutableListOf<String>("batch size ${item.batchSize} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledViolationTemplateSummaryLengthRule(var maxLength: Int) : ScheduledViolationTemplateRule {
    override fun check(item: ScheduledViolationTemplate): MutableList<String> {
        val size = item.summary.length
        if (size > maxLength) {
            return mutableListOf<String>("summary is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled violation template. */
data class ScheduledViolationTemplateValidator(var rules: MutableList<ScheduledViolationTemplateRule>) {
    fun validate(item: ScheduledViolationTemplate): MutableList<String> {
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

    fun requireValid(item: ScheduledViolationTemplate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledViolationTemplate>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledViolationTemplateValidator(): ScheduledViolationTemplateValidator {
    return ScheduledViolationTemplateValidator(
        mutableListOf<ScheduledViolationTemplateRule>(
            ScheduledViolationTemplateRequiredNameRule("scheduled violation template needs an entry"),
            ScheduledViolationTemplateArchivedGuardRule(32),
            ScheduledViolationTemplateSummaryLengthRule(32),
        ),
    )
}
