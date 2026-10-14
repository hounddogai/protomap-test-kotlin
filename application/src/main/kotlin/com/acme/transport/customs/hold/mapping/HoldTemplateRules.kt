package com.acme.transport.customs.hold.mapping

import com.acme.transport.kit.text.normalize

/** A rule a hold template must pass. Check returns its problems, or none. */
interface HoldTemplateRule {
    fun check(item: HoldTemplate): MutableList<String>
}

data class HoldTemplateRequiredRouteCodeRule(var message: String) : HoldTemplateRule {
    override fun check(item: HoldTemplate): MutableList<String> {
        if (item.routeCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class HoldTemplateAllowedCodeRule(var allowed: MutableList<String>) : HoldTemplateRule {
    override fun check(item: HoldTemplate): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class HoldTemplateDiscountRangeRule(var min: Double, var max: Double) : HoldTemplateRule {
    override fun check(item: HoldTemplate): MutableList<String> {
        val value = item.discount
        if (value < min || value > max) {
            return mutableListOf<String>("discount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class HoldTemplateRouteCodeLengthRule(var maxLength: Int) : HoldTemplateRule {
    override fun check(item: HoldTemplate): MutableList<String> {
        val size = item.routeCode.length
        if (size > maxLength) {
            return mutableListOf<String>("route code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class HoldTemplateNameLengthRule(var maxLength: Int) : HoldTemplateRule {
    override fun check(item: HoldTemplate): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class HoldTemplateRequiredContactEmailRule(var message: String) : HoldTemplateRule {
    override fun check(item: HoldTemplate): MutableList<String> {
        if (item.contactEmail.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a hold template. */
data class HoldTemplateValidator(var rules: MutableList<HoldTemplateRule>) {
    fun validate(item: HoldTemplate): MutableList<String> {
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

    fun requireValid(item: HoldTemplate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<HoldTemplate>): MutableMap<String, MutableList<String>> {
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

fun defaultHoldTemplateValidator(): HoldTemplateValidator {
    return HoldTemplateValidator(
        mutableListOf<HoldTemplateRule>(
            HoldTemplateRequiredRouteCodeRule("hold template needs this field"),
            HoldTemplateAllowedCodeRule(mutableListOf<String>("adaptive", "rolling", "weighted")),
            HoldTemplateDiscountRangeRule(2.6, 3170.5),
            HoldTemplateRouteCodeLengthRule(64),
            HoldTemplateNameLengthRule(32),
            HoldTemplateRequiredContactEmailRule("hold template needs a value"),
        ),
    )
}
