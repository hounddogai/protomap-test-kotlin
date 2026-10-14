package com.acme.transport.terminal.crossdock.schedule

import com.acme.transport.kit.text.normalize

/** A rule a tiered cross dock rule must pass. Check returns its problems, or none. */
interface TieredCrossDockRuleRule {
    fun check(item: TieredCrossDockRule): MutableList<String>
}

data class TieredCrossDockRuleRequiredCodeRule(var message: String) : TieredCrossDockRuleRule {
    override fun check(item: TieredCrossDockRule): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredCrossDockRuleTimezoneLengthRule(var maxLength: Int) : TieredCrossDockRuleRule {
    override fun check(item: TieredCrossDockRule): MutableList<String> {
        val size = item.timezone.length
        if (size > maxLength) {
            return mutableListOf<String>("timezone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredCrossDockRuleCodeLengthRule(var maxLength: Int) : TieredCrossDockRuleRule {
    override fun check(item: TieredCrossDockRule): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredCrossDockRuleAllowedCodeRule(var allowed: MutableList<String>) : TieredCrossDockRuleRule {
    override fun check(item: TieredCrossDockRule): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class TieredCrossDockRuleExpiresAtRangeRule(var min: Int, var max: Int) : TieredCrossDockRuleRule {
    override fun check(item: TieredCrossDockRule): MutableList<String> {
        val value = item.expiresAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("expires at $value is below $min")
        }
        if (value > max) {
            problems.add("expires at $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a tiered cross dock rule. */
data class TieredCrossDockRuleValidator(var rules: MutableList<TieredCrossDockRuleRule>) {
    fun validate(item: TieredCrossDockRule): MutableList<String> {
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

    fun requireValid(item: TieredCrossDockRule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredCrossDockRule>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredCrossDockRuleValidator(): TieredCrossDockRuleValidator {
    return TieredCrossDockRuleValidator(
        mutableListOf<TieredCrossDockRuleRule>(
            TieredCrossDockRuleRequiredCodeRule("tiered cross dock rule needs a value"),
            TieredCrossDockRuleTimezoneLengthRule(86400),
            TieredCrossDockRuleCodeLengthRule(16),
            TieredCrossDockRuleAllowedCodeRule(mutableListOf<String>("manual", "regional", "batched")),
            TieredCrossDockRuleExpiresAtRangeRule(64, 86400),
        ),
    )
}
