package com.acme.transport.network.transittime.slot

/** A rule a transit time template must pass. Check returns its problems, or none. */
interface TransitTimeTemplateRule {
    fun check(item: TransitTimeTemplate): MutableList<String>
}

data class TransitTimeTemplateRequiredCodeRule(var message: String) : TransitTimeTemplateRule {
    override fun check(item: TransitTimeTemplate): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TransitTimeTemplateNameLengthRule(var maxLength: Int) : TransitTimeTemplateRule {
    override fun check(item: TransitTimeTemplate): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TransitTimeTemplateRequiredShiftCodeRule(var message: String) : TransitTimeTemplateRule {
    override fun check(item: TransitTimeTemplate): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TransitTimeTemplateDescriptionLengthRule(var maxLength: Int) : TransitTimeTemplateRule {
    override fun check(item: TransitTimeTemplate): MutableList<String> {
        val size = item.description.length
        if (size > maxLength) {
            return mutableListOf<String>("description is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TransitTimeTemplateCodePrefixRule(var prefix: String) : TransitTimeTemplateRule {
    override fun check(item: TransitTimeTemplate): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TransitTimeTemplateRequiredNameRule(var message: String) : TransitTimeTemplateRule {
    override fun check(item: TransitTimeTemplate): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a transit time template. */
data class TransitTimeTemplateValidator(var rules: MutableList<TransitTimeTemplateRule>) {
    fun validate(item: TransitTimeTemplate): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TransitTimeTemplate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TransitTimeTemplate>): MutableMap<String, MutableList<String>> {
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

fun defaultTransitTimeTemplateValidator(): TransitTimeTemplateValidator {
    return TransitTimeTemplateValidator(
        mutableListOf<TransitTimeTemplateRule>(
            TransitTimeTemplateRequiredCodeRule("transit time template needs a value"),
            TransitTimeTemplateNameLengthRule(1000),
            TransitTimeTemplateRequiredShiftCodeRule("transit time template needs an entry"),
            TransitTimeTemplateDescriptionLengthRule(80),
            TransitTimeTemplateCodePrefixRule("OPS"),
            TransitTimeTemplateRequiredNameRule("transit time template needs this field"),
        ),
    )
}
