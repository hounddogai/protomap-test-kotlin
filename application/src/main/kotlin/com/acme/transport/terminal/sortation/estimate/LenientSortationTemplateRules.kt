package com.acme.transport.terminal.sortation.estimate

/** A rule a lenient sortation template must pass. Check returns its problems, or none. */
interface LenientSortationTemplateRule {
    fun check(item: LenientSortationTemplate): MutableList<String>
}

data class LenientSortationTemplateCodePrefixRule(var prefix: String) : LenientSortationTemplateRule {
    override fun check(item: LenientSortationTemplate): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LenientSortationTemplateRequiredPhoneRule(var message: String) : LenientSortationTemplateRule {
    override fun check(item: LenientSortationTemplate): MutableList<String> {
        if (item.phone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientSortationTemplateRequiredCodeRule(var message: String) : LenientSortationTemplateRule {
    override fun check(item: LenientSortationTemplate): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient sortation template. */
data class LenientSortationTemplateValidator(var rules: MutableList<LenientSortationTemplateRule>) {
    fun validate(item: LenientSortationTemplate): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LenientSortationTemplate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientSortationTemplate>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientSortationTemplateValidator(): LenientSortationTemplateValidator {
    return LenientSortationTemplateValidator(
        mutableListOf<LenientSortationTemplateRule>(
            LenientSortationTemplateCodePrefixRule("ACM"),
            LenientSortationTemplateRequiredPhoneRule("lenient sortation template needs an entry"),
            LenientSortationTemplateRequiredCodeRule("lenient sortation template needs this field"),
        ),
    )
}
