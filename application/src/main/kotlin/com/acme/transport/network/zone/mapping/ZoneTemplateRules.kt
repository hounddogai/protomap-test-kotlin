package com.acme.transport.network.zone.mapping

import com.acme.transport.kit.text.normalize

/** A rule a zone template must pass. Check returns its problems, or none. */
interface ZoneTemplateRule {
    fun check(item: ZoneTemplate): MutableList<String>
}

data class ZoneTemplateCodePrefixRule(var prefix: String) : ZoneTemplateRule {
    override fun check(item: ZoneTemplate): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ZoneTemplateNameLengthRule(var maxLength: Int) : ZoneTemplateRule {
    override fun check(item: ZoneTemplate): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ZoneTemplateAllowedSourceRule(var allowed: MutableList<String>) : ZoneTemplateRule {
    override fun check(item: ZoneTemplate): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.source))) {
            return mutableListOf<String>("source ${item.source} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class ZoneTemplateRequiredNameRule(var message: String) : ZoneTemplateRule {
    override fun check(item: ZoneTemplate): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ZoneTemplateRequiredExternalRefRule(var message: String) : ZoneTemplateRule {
    override fun check(item: ZoneTemplate): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a zone template. */
data class ZoneTemplateValidator(var rules: MutableList<ZoneTemplateRule>) {
    fun validate(item: ZoneTemplate): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ZoneTemplate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ZoneTemplate>): MutableMap<String, MutableList<String>> {
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

fun defaultZoneTemplateValidator(): ZoneTemplateValidator {
    return ZoneTemplateValidator(
        mutableListOf<ZoneTemplateRule>(
            ZoneTemplateCodePrefixRule("Q"),
            ZoneTemplateNameLengthRule(1000),
            ZoneTemplateAllowedSourceRule(mutableListOf<String>("weighted", "tiered")),
            ZoneTemplateRequiredNameRule("zone template needs an entry"),
            ZoneTemplateRequiredExternalRefRule("zone template needs this field"),
        ),
    )
}
