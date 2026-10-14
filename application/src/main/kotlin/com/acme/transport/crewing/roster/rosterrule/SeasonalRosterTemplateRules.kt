package com.acme.transport.crewing.roster.rosterrule

/** A rule a seasonal roster template must pass. Check returns its problems, or none. */
interface SeasonalRosterTemplateRule {
    fun check(item: SeasonalRosterTemplate): MutableList<String>
}

data class SeasonalRosterTemplateCodeLengthRule(var maxLength: Int) : SeasonalRosterTemplateRule {
    override fun check(item: SeasonalRosterTemplate): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalRosterTemplateRequiredLabelRule(var message: String) : SeasonalRosterTemplateRule {
    override fun check(item: SeasonalRosterTemplate): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalRosterTemplateRequiredPhoneRule(var message: String) : SeasonalRosterTemplateRule {
    override fun check(item: SeasonalRosterTemplate): MutableList<String> {
        if (item.phone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalRosterTemplateCodePrefixRule(var prefix: String) : SeasonalRosterTemplateRule {
    override fun check(item: SeasonalRosterTemplate): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal roster template. */
data class SeasonalRosterTemplateValidator(var rules: MutableList<SeasonalRosterTemplateRule>) {
    fun validate(item: SeasonalRosterTemplate): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SeasonalRosterTemplate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalRosterTemplate>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalRosterTemplateValidator(): SeasonalRosterTemplateValidator {
    return SeasonalRosterTemplateValidator(
        mutableListOf<SeasonalRosterTemplateRule>(
            SeasonalRosterTemplateCodeLengthRule(255),
            SeasonalRosterTemplateRequiredLabelRule("seasonal roster template needs this field"),
            SeasonalRosterTemplateRequiredPhoneRule("seasonal roster template needs this field"),
            SeasonalRosterTemplateCodePrefixRule("Q"),
        ),
    )
}
