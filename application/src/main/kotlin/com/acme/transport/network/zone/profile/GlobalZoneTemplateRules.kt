package com.acme.transport.network.zone.profile

import com.acme.transport.kit.text.normalize

/** A rule a global zone template must pass. Check returns its problems, or none. */
interface GlobalZoneTemplateRule {
    fun check(item: GlobalZoneTemplate): MutableList<String>
}

data class GlobalZoneTemplateCodePrefixRule(var prefix: String) : GlobalZoneTemplateRule {
    override fun check(item: GlobalZoneTemplate): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class GlobalZoneTemplateChecksumLengthRule(var maxLength: Int) : GlobalZoneTemplateRule {
    override fun check(item: GlobalZoneTemplate): MutableList<String> {
        val size = item.checksum.length
        if (size > maxLength) {
            return mutableListOf<String>("checksum is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalZoneTemplateLoadKgRangeRule(var min: Double, var max: Double) : GlobalZoneTemplateRule {
    override fun check(item: GlobalZoneTemplate): MutableList<String> {
        val value = item.loadKg
        if (value < min || value > max) {
            return mutableListOf<String>("load kg $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class GlobalZoneTemplateRequiredTitleRule(var message: String) : GlobalZoneTemplateRule {
    override fun check(item: GlobalZoneTemplate): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalZoneTemplateTitleLengthRule(var maxLength: Int) : GlobalZoneTemplateRule {
    override fun check(item: GlobalZoneTemplate): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalZoneTemplateAllowedChecksumRule(var allowed: MutableList<String>) : GlobalZoneTemplateRule {
    override fun check(item: GlobalZoneTemplate): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.checksum))) {
            return mutableListOf<String>("checksum ${item.checksum} is unknown")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global zone template. */
data class GlobalZoneTemplateValidator(var rules: MutableList<GlobalZoneTemplateRule>) {
    fun validate(item: GlobalZoneTemplate): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: GlobalZoneTemplate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalZoneTemplate>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalZoneTemplateValidator(): GlobalZoneTemplateValidator {
    return GlobalZoneTemplateValidator(
        mutableListOf<GlobalZoneTemplateRule>(
            GlobalZoneTemplateCodePrefixRule("Q"),
            GlobalZoneTemplateChecksumLengthRule(86400),
            GlobalZoneTemplateLoadKgRangeRule(3.2, 4296.2),
            GlobalZoneTemplateRequiredTitleRule("global zone template needs a value"),
            GlobalZoneTemplateTitleLengthRule(80),
            GlobalZoneTemplateAllowedChecksumRule(mutableListOf<String>("batched", "regional", "batched")),
        ),
    )
}
