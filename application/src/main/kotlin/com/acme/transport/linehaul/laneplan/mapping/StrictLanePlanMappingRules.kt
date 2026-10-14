package com.acme.transport.linehaul.laneplan.mapping

/** A rule a strict lane plan mapping must pass. Check returns its problems, or none. */
interface StrictLanePlanMappingRule {
    fun check(item: StrictLanePlanMapping): MutableList<String>
}

data class StrictLanePlanMappingCodePrefixRule(var prefix: String) : StrictLanePlanMappingRule {
    override fun check(item: StrictLanePlanMapping): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class StrictLanePlanMappingMarginRangeRule(var min: Double, var max: Double) : StrictLanePlanMappingRule {
    override fun check(item: StrictLanePlanMapping): MutableList<String> {
        val value = item.margin
        if (value < min || value > max) {
            return mutableListOf<String>("margin $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class StrictLanePlanMappingRequiredCategoryRule(var message: String) : StrictLanePlanMappingRule {
    override fun check(item: StrictLanePlanMapping): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictLanePlanMappingCategoryLengthRule(var maxLength: Int) : StrictLanePlanMappingRule {
    override fun check(item: StrictLanePlanMapping): MutableList<String> {
        val size = item.category.length
        if (size > maxLength) {
            return mutableListOf<String>("category is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictLanePlanMappingTitleLengthRule(var maxLength: Int) : StrictLanePlanMappingRule {
    override fun check(item: StrictLanePlanMapping): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictLanePlanMappingRequiredCodeRule(var message: String) : StrictLanePlanMappingRule {
    override fun check(item: StrictLanePlanMapping): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict lane plan mapping. */
data class StrictLanePlanMappingValidator(var rules: MutableList<StrictLanePlanMappingRule>) {
    fun validate(item: StrictLanePlanMapping): MutableList<String> {
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

    fun requireValid(item: StrictLanePlanMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictLanePlanMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictLanePlanMappingValidator(): StrictLanePlanMappingValidator {
    return StrictLanePlanMappingValidator(
        mutableListOf<StrictLanePlanMappingRule>(
            StrictLanePlanMappingCodePrefixRule("REF"),
            StrictLanePlanMappingMarginRangeRule(5.4, 4120.8),
            StrictLanePlanMappingRequiredCategoryRule("strict lane plan mapping needs a value"),
            StrictLanePlanMappingCategoryLengthRule(64),
            StrictLanePlanMappingTitleLengthRule(3600),
            StrictLanePlanMappingRequiredCodeRule("strict lane plan mapping needs a value"),
        ),
    )
}
