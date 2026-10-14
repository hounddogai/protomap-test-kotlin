package com.acme.transport.freightpay.audit.legacyauditpolicy

/** A rule a adaptive audit template must pass. Check returns its problems, or none. */
interface AdaptiveAuditTemplateRule {
    fun check(item: AdaptiveAuditTemplate): MutableList<String>
}

data class AdaptiveAuditTemplateMinimumRangeRule(var min: Double, var max: Double) : AdaptiveAuditTemplateRule {
    override fun check(item: AdaptiveAuditTemplate): MutableList<String> {
        val value = item.minimum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("minimum $value is below $min")
        }
        if (value > max) {
            problems.add("minimum $value is above $max")
        }
        return problems
    }
}

data class AdaptiveAuditTemplateCodeLengthRule(var maxLength: Int) : AdaptiveAuditTemplateRule {
    override fun check(item: AdaptiveAuditTemplate): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveAuditTemplateRequiredDepotRule(var message: String) : AdaptiveAuditTemplateRule {
    override fun check(item: AdaptiveAuditTemplate): MutableList<String> {
        if (item.depot.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive audit template. */
data class AdaptiveAuditTemplateValidator(var rules: MutableList<AdaptiveAuditTemplateRule>) {
    fun validate(item: AdaptiveAuditTemplate): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AdaptiveAuditTemplate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveAuditTemplate>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveAuditTemplateValidator(): AdaptiveAuditTemplateValidator {
    return AdaptiveAuditTemplateValidator(
        mutableListOf<AdaptiveAuditTemplateRule>(
            AdaptiveAuditTemplateMinimumRangeRule(9.8, 664.3),
            AdaptiveAuditTemplateCodeLengthRule(86400),
            AdaptiveAuditTemplateRequiredDepotRule("adaptive audit template needs this field"),
        ),
    )
}
