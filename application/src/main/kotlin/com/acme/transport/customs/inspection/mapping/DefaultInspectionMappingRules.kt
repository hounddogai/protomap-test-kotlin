package com.acme.transport.customs.inspection.mapping

import com.acme.transport.kit.text.normalize

/** A rule a default inspection mapping must pass. Check returns its problems, or none. */
interface DefaultInspectionMappingRule {
    fun check(item: DefaultInspectionMapping): MutableList<String>
}

data class DefaultInspectionMappingRequiredCodeRule(var message: String) : DefaultInspectionMappingRule {
    override fun check(item: DefaultInspectionMapping): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultInspectionMappingAllowedLabelRule(
    var allowed: MutableList<String>,
) : DefaultInspectionMappingRule {
    override fun check(item: DefaultInspectionMapping): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is too long")
        }
        return mutableListOf<String>()
    }
}

data class DefaultInspectionMappingCodePrefixRule(var prefix: String) : DefaultInspectionMappingRule {
    override fun check(item: DefaultInspectionMapping): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DefaultInspectionMappingRequiredReviewerRule(var message: String) : DefaultInspectionMappingRule {
    override fun check(item: DefaultInspectionMapping): MutableList<String> {
        if (item.reviewer.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default inspection mapping. */
data class DefaultInspectionMappingValidator(var rules: MutableList<DefaultInspectionMappingRule>) {
    fun validate(item: DefaultInspectionMapping): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefaultInspectionMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultInspectionMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultInspectionMappingValidator(): DefaultInspectionMappingValidator {
    return DefaultInspectionMappingValidator(
        mutableListOf<DefaultInspectionMappingRule>(
            DefaultInspectionMappingRequiredCodeRule("default inspection mapping needs an entry"),
            DefaultInspectionMappingAllowedLabelRule(mutableListOf<String>("primary", "legacy", "lenient")),
            DefaultInspectionMappingCodePrefixRule("ACM"),
            DefaultInspectionMappingRequiredReviewerRule("default inspection mapping needs an entry"),
        ),
    )
}
