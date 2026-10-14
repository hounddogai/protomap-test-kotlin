package com.acme.transport.freightpay.fuelsurcharge.notice

import com.acme.transport.kit.text.normalize

/** A rule a pending fuel surcharge template must pass. Check returns its problems, or none. */
interface PendingFuelSurchargeTemplateRule {
    fun check(item: PendingFuelSurchargeTemplate): MutableList<String>
}

data class PendingFuelSurchargeTemplateCodePrefixRule(var prefix: String) : PendingFuelSurchargeTemplateRule {
    override fun check(item: PendingFuelSurchargeTemplate): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class PendingFuelSurchargeTemplateCodeLengthRule(var maxLength: Int) : PendingFuelSurchargeTemplateRule {
    override fun check(item: PendingFuelSurchargeTemplate): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingFuelSurchargeTemplateRatioRangeRule(
    var min: Double,
    var max: Double,
) : PendingFuelSurchargeTemplateRule {
    override fun check(item: PendingFuelSurchargeTemplate): MutableList<String> {
        val value = item.ratio
        if (value < min || value > max) {
            return mutableListOf<String>("ratio $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PendingFuelSurchargeTemplateAllowedLocaleRule(
    var allowed: MutableList<String>,
) : PendingFuelSurchargeTemplateRule {
    override fun check(item: PendingFuelSurchargeTemplate): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.locale))) {
            return mutableListOf<String>("locale ${item.locale} is stale")
        }
        return mutableListOf<String>()
    }
}

data class PendingFuelSurchargeTemplateVersionRangeRule(
    var min: Int,
    var max: Int,
) : PendingFuelSurchargeTemplateRule {
    override fun check(item: PendingFuelSurchargeTemplate): MutableList<String> {
        val value = item.version
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("version $value is below $min")
        }
        if (value > max) {
            problems.add("version $value is above $max")
        }
        return problems
    }
}

data class PendingFuelSurchargeTemplateRequiredLocaleRule(
    var message: String,
) : PendingFuelSurchargeTemplateRule {
    override fun check(item: PendingFuelSurchargeTemplate): MutableList<String> {
        if (item.locale.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending fuel surcharge template. */
data class PendingFuelSurchargeTemplateValidator(var rules: MutableList<PendingFuelSurchargeTemplateRule>) {
    fun validate(item: PendingFuelSurchargeTemplate): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PendingFuelSurchargeTemplate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingFuelSurchargeTemplate>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingFuelSurchargeTemplateValidator(): PendingFuelSurchargeTemplateValidator {
    return PendingFuelSurchargeTemplateValidator(
        mutableListOf<PendingFuelSurchargeTemplateRule>(
            PendingFuelSurchargeTemplateCodePrefixRule("ACM"),
            PendingFuelSurchargeTemplateCodeLengthRule(1000),
            PendingFuelSurchargeTemplateRatioRangeRule(6.8, 4569.2),
            PendingFuelSurchargeTemplateAllowedLocaleRule(mutableListOf<String>("lenient", "capped")),
            PendingFuelSurchargeTemplateVersionRangeRule(64, 255),
            PendingFuelSurchargeTemplateRequiredLocaleRule("pending fuel surcharge template needs a value"),
        ),
    )
}
