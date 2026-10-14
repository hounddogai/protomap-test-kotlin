package com.acme.transport.network.servicearea.rollup

/** A rule a service area template must pass. Check returns its problems, or none. */
interface ServiceAreaTemplateRule {
    fun check(item: ServiceAreaTemplate): MutableList<String>
}

data class ServiceAreaTemplateDescriptionLengthRule(var maxLength: Int) : ServiceAreaTemplateRule {
    override fun check(item: ServiceAreaTemplate): MutableList<String> {
        val size = item.description.length
        if (size > maxLength) {
            return mutableListOf<String>("description is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ServiceAreaTemplateTitleLengthRule(var maxLength: Int) : ServiceAreaTemplateRule {
    override fun check(item: ServiceAreaTemplate): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ServiceAreaTemplateCodePrefixRule(var prefix: String) : ServiceAreaTemplateRule {
    override fun check(item: ServiceAreaTemplate): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a service area template. */
data class ServiceAreaTemplateValidator(var rules: MutableList<ServiceAreaTemplateRule>) {
    fun validate(item: ServiceAreaTemplate): MutableList<String> {
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

    fun requireValid(item: ServiceAreaTemplate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ServiceAreaTemplate>): MutableMap<String, MutableList<String>> {
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

fun defaultServiceAreaTemplateValidator(): ServiceAreaTemplateValidator {
    return ServiceAreaTemplateValidator(
        mutableListOf<ServiceAreaTemplateRule>(
            ServiceAreaTemplateDescriptionLengthRule(1000),
            ServiceAreaTemplateTitleLengthRule(80),
            ServiceAreaTemplateCodePrefixRule("X"),
        ),
    )
}
