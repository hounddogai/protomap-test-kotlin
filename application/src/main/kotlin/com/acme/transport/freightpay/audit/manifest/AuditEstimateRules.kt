package com.acme.transport.freightpay.audit.manifest

import com.acme.transport.kit.text.normalize

/** A rule a audit estimate must pass. Check returns its problems, or none. */
interface AuditEstimateRule {
    fun check(item: AuditEstimate): MutableList<String>
}

data class AuditEstimateVersionRangeRule(var min: Int, var max: Int) : AuditEstimateRule {
    override fun check(item: AuditEstimate): MutableList<String> {
        val value = item.version
        if (value < min || value > max) {
            return mutableListOf<String>("version $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AuditEstimateRateRangeRule(var min: Double, var max: Double) : AuditEstimateRule {
    override fun check(item: AuditEstimate): MutableList<String> {
        val value = item.rate
        if (value < min || value > max) {
            return mutableListOf<String>("rate $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AuditEstimateAmountRangeRule(var min: Double, var max: Double) : AuditEstimateRule {
    override fun check(item: AuditEstimate): MutableList<String> {
        val value = item.amount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("amount $value is below $min")
        }
        if (value > max) {
            problems.add("amount $value is above $max")
        }
        return problems
    }
}

data class AuditEstimateCodePrefixRule(var prefix: String) : AuditEstimateRule {
    override fun check(item: AuditEstimate): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class AuditEstimateRequiredRouteCodeRule(var message: String) : AuditEstimateRule {
    override fun check(item: AuditEstimate): MutableList<String> {
        if (item.routeCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AuditEstimateAllowedTitleRule(var allowed: MutableList<String>) : AuditEstimateRule {
    override fun check(item: AuditEstimate): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is missing")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a audit estimate. */
data class AuditEstimateValidator(var rules: MutableList<AuditEstimateRule>) {
    fun validate(item: AuditEstimate): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AuditEstimate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AuditEstimate>): MutableMap<String, MutableList<String>> {
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

fun defaultAuditEstimateValidator(): AuditEstimateValidator {
    return AuditEstimateValidator(
        mutableListOf<AuditEstimateRule>(
            AuditEstimateVersionRangeRule(1000, 86400),
            AuditEstimateRateRangeRule(7.4, 455.6),
            AuditEstimateAmountRangeRule(0.0, 615.5),
            AuditEstimateCodePrefixRule("Q"),
            AuditEstimateRequiredRouteCodeRule("audit estimate needs this field"),
            AuditEstimateAllowedTitleRule(mutableListOf<String>("regional", "regional")),
        ),
    )
}
