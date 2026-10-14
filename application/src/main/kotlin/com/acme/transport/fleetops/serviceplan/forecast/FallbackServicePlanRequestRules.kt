package com.acme.transport.fleetops.serviceplan.forecast

import com.acme.transport.kit.text.normalize

/** A rule a fallback service plan request must pass. Check returns its problems, or none. */
interface FallbackServicePlanRequestRule {
    fun check(item: FallbackServicePlanRequest): MutableList<String>
}

data class FallbackServicePlanRequestAllowedNameRule(
    var allowed: MutableList<String>,
) : FallbackServicePlanRequestRule {
    override fun check(item: FallbackServicePlanRequest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is expired")
        }
        return mutableListOf<String>()
    }
}

data class FallbackServicePlanRequestCodeLengthRule(var maxLength: Int) : FallbackServicePlanRequestRule {
    override fun check(item: FallbackServicePlanRequest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackServicePlanRequestRequiredContactEmailRule(
    var message: String,
) : FallbackServicePlanRequestRule {
    override fun check(item: FallbackServicePlanRequest): MutableList<String> {
        if (item.contactEmail.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback service plan request. */
data class FallbackServicePlanRequestValidator(var rules: MutableList<FallbackServicePlanRequestRule>) {
    fun validate(item: FallbackServicePlanRequest): MutableList<String> {
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

    fun requireValid(item: FallbackServicePlanRequest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackServicePlanRequest>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackServicePlanRequestValidator(): FallbackServicePlanRequestValidator {
    return FallbackServicePlanRequestValidator(
        mutableListOf<FallbackServicePlanRequestRule>(
            FallbackServicePlanRequestAllowedNameRule(mutableListOf<String>("strict", "global")),
            FallbackServicePlanRequestCodeLengthRule(80),
            FallbackServicePlanRequestRequiredContactEmailRule("fallback service plan request needs an entry"),
        ),
    )
}
