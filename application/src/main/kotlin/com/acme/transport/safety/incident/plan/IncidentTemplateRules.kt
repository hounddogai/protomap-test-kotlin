package com.acme.transport.safety.incident.plan

import com.acme.transport.kit.text.normalize

/** A rule a incident template must pass. Check returns its problems, or none. */
interface IncidentTemplateRule {
    fun check(item: IncidentTemplate): MutableList<String>
}

data class IncidentTemplateRequiredCodeRule(var message: String) : IncidentTemplateRule {
    override fun check(item: IncidentTemplate): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class IncidentTemplateCodePrefixRule(var prefix: String) : IncidentTemplateRule {
    override fun check(item: IncidentTemplate): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class IncidentTemplateAllowedTitleRule(var allowed: MutableList<String>) : IncidentTemplateRule {
    override fun check(item: IncidentTemplate): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class IncidentTemplateRequiredTitleRule(var message: String) : IncidentTemplateRule {
    override fun check(item: IncidentTemplate): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a incident template. */
data class IncidentTemplateValidator(var rules: MutableList<IncidentTemplateRule>) {
    fun validate(item: IncidentTemplate): MutableList<String> {
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

    fun requireValid(item: IncidentTemplate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<IncidentTemplate>): MutableMap<String, MutableList<String>> {
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

fun defaultIncidentTemplateValidator(): IncidentTemplateValidator {
    return IncidentTemplateValidator(
        mutableListOf<IncidentTemplateRule>(
            IncidentTemplateRequiredCodeRule("incident template needs this field"),
            IncidentTemplateCodePrefixRule("Q"),
            IncidentTemplateAllowedTitleRule(mutableListOf<String>("capped")),
            IncidentTemplateRequiredTitleRule("incident template needs an entry"),
        ),
    )
}
