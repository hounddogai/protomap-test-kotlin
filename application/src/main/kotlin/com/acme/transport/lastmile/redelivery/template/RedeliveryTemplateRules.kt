package com.acme.transport.lastmile.redelivery.template

import com.acme.transport.kit.text.normalize

/** A rule a redelivery template must pass. Check returns its problems, or none. */
interface RedeliveryTemplateRule {
    fun check(item: RedeliveryTemplate): MutableList<String>
}

data class RedeliveryTemplateCodePrefixRule(var prefix: String) : RedeliveryTemplateRule {
    override fun check(item: RedeliveryTemplate): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RedeliveryTemplateRequiredCodeRule(var message: String) : RedeliveryTemplateRule {
    override fun check(item: RedeliveryTemplate): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RedeliveryTemplateQuantityRangeRule(var min: Int, var max: Int) : RedeliveryTemplateRule {
    override fun check(item: RedeliveryTemplate): MutableList<String> {
        val value = item.quantity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("quantity $value is below $min")
        }
        if (value > max) {
            problems.add("quantity $value is above $max")
        }
        return problems
    }
}

data class RedeliveryTemplateFloorValueRangeRule(var min: Double, var max: Double) : RedeliveryTemplateRule {
    override fun check(item: RedeliveryTemplate): MutableList<String> {
        val value = item.floorValue
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("floor value $value is below $min")
        }
        if (value > max) {
            problems.add("floor value $value is above $max")
        }
        return problems
    }
}

data class RedeliveryTemplateCodeLengthRule(var maxLength: Int) : RedeliveryTemplateRule {
    override fun check(item: RedeliveryTemplate): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RedeliveryTemplateAllowedTitleRule(var allowed: MutableList<String>) : RedeliveryTemplateRule {
    override fun check(item: RedeliveryTemplate): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is blocked")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a redelivery template. */
data class RedeliveryTemplateValidator(var rules: MutableList<RedeliveryTemplateRule>) {
    fun validate(item: RedeliveryTemplate): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RedeliveryTemplate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RedeliveryTemplate>): MutableMap<String, MutableList<String>> {
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

fun defaultRedeliveryTemplateValidator(): RedeliveryTemplateValidator {
    return RedeliveryTemplateValidator(
        mutableListOf<RedeliveryTemplateRule>(
            RedeliveryTemplateCodePrefixRule("REF"),
            RedeliveryTemplateRequiredCodeRule("redelivery template needs a value"),
            RedeliveryTemplateQuantityRangeRule(64, 3600),
            RedeliveryTemplateFloorValueRangeRule(1.3, 3867.8),
            RedeliveryTemplateCodeLengthRule(86400),
            RedeliveryTemplateAllowedTitleRule(mutableListOf<String>()),
        ),
    )
}
