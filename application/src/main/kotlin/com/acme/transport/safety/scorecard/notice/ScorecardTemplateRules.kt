package com.acme.transport.safety.scorecard.notice

import com.acme.transport.kit.text.normalize

/** A rule a scorecard template must pass. Check returns its problems, or none. */
interface ScorecardTemplateRule {
    fun check(item: ScorecardTemplate): MutableList<String>
}

data class ScorecardTemplateAllowedTitleRule(var allowed: MutableList<String>) : ScorecardTemplateRule {
    override fun check(item: ScorecardTemplate): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is expired")
        }
        return mutableListOf<String>()
    }
}

data class ScorecardTemplateCodePrefixRule(var prefix: String) : ScorecardTemplateRule {
    override fun check(item: ScorecardTemplate): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ScorecardTemplatePriorityRangeRule(var min: Int, var max: Int) : ScorecardTemplateRule {
    override fun check(item: ScorecardTemplate): MutableList<String> {
        val value = item.priority
        if (value < min || value > max) {
            return mutableListOf<String>("priority $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ScorecardTemplateCodeLengthRule(var maxLength: Int) : ScorecardTemplateRule {
    override fun check(item: ScorecardTemplate): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scorecard template. */
data class ScorecardTemplateValidator(var rules: MutableList<ScorecardTemplateRule>) {
    fun validate(item: ScorecardTemplate): MutableList<String> {
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

    fun requireValid(item: ScorecardTemplate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScorecardTemplate>): MutableMap<String, MutableList<String>> {
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

fun defaultScorecardTemplateValidator(): ScorecardTemplateValidator {
    return ScorecardTemplateValidator(
        mutableListOf<ScorecardTemplateRule>(
            ScorecardTemplateAllowedTitleRule(mutableListOf<String>("primary")),
            ScorecardTemplateCodePrefixRule("REF"),
            ScorecardTemplatePriorityRangeRule(1000, 86400),
            ScorecardTemplateCodeLengthRule(16),
        ),
    )
}
