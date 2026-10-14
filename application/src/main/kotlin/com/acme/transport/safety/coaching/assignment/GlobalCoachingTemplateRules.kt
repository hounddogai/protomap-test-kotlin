package com.acme.transport.safety.coaching.assignment

/** A rule a global coaching template must pass. Check returns its problems, or none. */
interface GlobalCoachingTemplateRule {
    fun check(item: GlobalCoachingTemplate): MutableList<String>
}

data class GlobalCoachingTemplateDescriptionLengthRule(var maxLength: Int) : GlobalCoachingTemplateRule {
    override fun check(item: GlobalCoachingTemplate): MutableList<String> {
        val size = item.description.length
        if (size > maxLength) {
            return mutableListOf<String>("description is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalCoachingTemplateRequiredCodeRule(var message: String) : GlobalCoachingTemplateRule {
    override fun check(item: GlobalCoachingTemplate): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalCoachingTemplateRequiredLabelRule(var message: String) : GlobalCoachingTemplateRule {
    override fun check(item: GlobalCoachingTemplate): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalCoachingTemplateLabelLengthRule(var maxLength: Int) : GlobalCoachingTemplateRule {
    override fun check(item: GlobalCoachingTemplate): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalCoachingTemplateRequiredDescriptionRule(var message: String) : GlobalCoachingTemplateRule {
    override fun check(item: GlobalCoachingTemplate): MutableList<String> {
        if (item.description.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global coaching template. */
data class GlobalCoachingTemplateValidator(var rules: MutableList<GlobalCoachingTemplateRule>) {
    fun validate(item: GlobalCoachingTemplate): MutableList<String> {
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

    fun requireValid(item: GlobalCoachingTemplate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalCoachingTemplate>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalCoachingTemplateValidator(): GlobalCoachingTemplateValidator {
    return GlobalCoachingTemplateValidator(
        mutableListOf<GlobalCoachingTemplateRule>(
            GlobalCoachingTemplateDescriptionLengthRule(86400),
            GlobalCoachingTemplateRequiredCodeRule("global coaching template needs a value"),
            GlobalCoachingTemplateRequiredLabelRule("global coaching template needs this field"),
            GlobalCoachingTemplateLabelLengthRule(120),
            GlobalCoachingTemplateRequiredDescriptionRule("global coaching template needs an entry"),
        ),
    )
}
