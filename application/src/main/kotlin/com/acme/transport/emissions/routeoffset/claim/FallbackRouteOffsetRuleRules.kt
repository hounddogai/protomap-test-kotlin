package com.acme.transport.emissions.routeoffset.claim

/** A rule a fallback route offset rule must pass. Check returns its problems, or none. */
interface FallbackRouteOffsetRuleRule {
    fun check(item: FallbackRouteOffsetRule): MutableList<String>
}

data class FallbackRouteOffsetRuleCodeLengthRule(var maxLength: Int) : FallbackRouteOffsetRuleRule {
    override fun check(item: FallbackRouteOffsetRule): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackRouteOffsetRuleRequiredCodeRule(var message: String) : FallbackRouteOffsetRuleRule {
    override fun check(item: FallbackRouteOffsetRule): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackRouteOffsetRuleTitleLengthRule(var maxLength: Int) : FallbackRouteOffsetRuleRule {
    override fun check(item: FallbackRouteOffsetRule): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackRouteOffsetRuleRequiredLabelRule(var message: String) : FallbackRouteOffsetRuleRule {
    override fun check(item: FallbackRouteOffsetRule): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback route offset rule. */
data class FallbackRouteOffsetRuleValidator(var rules: MutableList<FallbackRouteOffsetRuleRule>) {
    fun validate(item: FallbackRouteOffsetRule): MutableList<String> {
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

    fun requireValid(item: FallbackRouteOffsetRule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackRouteOffsetRule>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackRouteOffsetRuleValidator(): FallbackRouteOffsetRuleValidator {
    return FallbackRouteOffsetRuleValidator(
        mutableListOf<FallbackRouteOffsetRuleRule>(
            FallbackRouteOffsetRuleCodeLengthRule(120),
            FallbackRouteOffsetRuleRequiredCodeRule("fallback route offset rule needs a value"),
            FallbackRouteOffsetRuleTitleLengthRule(64),
            FallbackRouteOffsetRuleRequiredLabelRule("fallback route offset rule needs this field"),
        ),
    )
}
