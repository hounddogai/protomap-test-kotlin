package com.acme.transport.safety.coaching.estimate

/** A rule a coaching template must pass. Check returns its problems, or none. */
interface CoachingTemplateRule {
    fun check(item: CoachingTemplate): MutableList<String>
}

data class CoachingTemplateRequiredCodeRule(var message: String) : CoachingTemplateRule {
    override fun check(item: CoachingTemplate): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CoachingTemplateRequiredOwnerIdRule(var message: String) : CoachingTemplateRule {
    override fun check(item: CoachingTemplate): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CoachingTemplateRequiredCurrencyRule(var message: String) : CoachingTemplateRule {
    override fun check(item: CoachingTemplate): MutableList<String> {
        if (item.currency.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CoachingTemplateOwnerIdLengthRule(var maxLength: Int) : CoachingTemplateRule {
    override fun check(item: CoachingTemplate): MutableList<String> {
        val size = item.ownerId.length
        if (size > maxLength) {
            return mutableListOf<String>("owner id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CoachingTemplateCurrencyLengthRule(var maxLength: Int) : CoachingTemplateRule {
    override fun check(item: CoachingTemplate): MutableList<String> {
        val size = item.currency.length
        if (size > maxLength) {
            return mutableListOf<String>("currency is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a coaching template. */
data class CoachingTemplateValidator(var rules: MutableList<CoachingTemplateRule>) {
    fun validate(item: CoachingTemplate): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CoachingTemplate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CoachingTemplate>): MutableMap<String, MutableList<String>> {
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

fun defaultCoachingTemplateValidator(): CoachingTemplateValidator {
    return CoachingTemplateValidator(
        mutableListOf<CoachingTemplateRule>(
            CoachingTemplateRequiredCodeRule("coaching template needs an entry"),
            CoachingTemplateRequiredOwnerIdRule("coaching template needs this field"),
            CoachingTemplateRequiredCurrencyRule("coaching template needs a value"),
            CoachingTemplateOwnerIdLengthRule(86400),
            CoachingTemplateCurrencyLengthRule(120),
        ),
    )
}
