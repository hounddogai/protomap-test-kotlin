package com.acme.transport.crewing.swap.rule

/** A rule a tiered swap rule must pass. Check returns its problems, or none. */
interface TieredSwapRuleRule {
    fun check(item: TieredSwapRule): MutableList<String>
}

data class TieredSwapRuleSegmentLengthRule(var maxLength: Int) : TieredSwapRuleRule {
    override fun check(item: TieredSwapRule): MutableList<String> {
        val size = item.segment.length
        if (size > maxLength) {
            return mutableListOf<String>("segment is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredSwapRuleUpdatedAtRangeRule(var min: Int, var max: Int) : TieredSwapRuleRule {
    override fun check(item: TieredSwapRule): MutableList<String> {
        val value = item.updatedAt
        if (value < min || value > max) {
            return mutableListOf<String>("updated at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TieredSwapRuleRequiredRegionRule(var message: String) : TieredSwapRuleRule {
    override fun check(item: TieredSwapRule): MutableList<String> {
        if (item.region.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredSwapRuleRegionLengthRule(var maxLength: Int) : TieredSwapRuleRule {
    override fun check(item: TieredSwapRule): MutableList<String> {
        val size = item.region.length
        if (size > maxLength) {
            return mutableListOf<String>("region is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredSwapRuleTitleLengthRule(var maxLength: Int) : TieredSwapRuleRule {
    override fun check(item: TieredSwapRule): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredSwapRuleExpiresAtRangeRule(var min: Int, var max: Int) : TieredSwapRuleRule {
    override fun check(item: TieredSwapRule): MutableList<String> {
        val value = item.expiresAt
        if (value < min || value > max) {
            return mutableListOf<String>("expires at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered swap rule. */
data class TieredSwapRuleValidator(var rules: MutableList<TieredSwapRuleRule>) {
    fun validate(item: TieredSwapRule): MutableList<String> {
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

    fun requireValid(item: TieredSwapRule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredSwapRule>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredSwapRuleValidator(): TieredSwapRuleValidator {
    return TieredSwapRuleValidator(
        mutableListOf<TieredSwapRuleRule>(
            TieredSwapRuleSegmentLengthRule(86400),
            TieredSwapRuleUpdatedAtRangeRule(80, 120),
            TieredSwapRuleRequiredRegionRule("tiered swap rule needs an entry"),
            TieredSwapRuleRegionLengthRule(255),
            TieredSwapRuleTitleLengthRule(1000),
            TieredSwapRuleExpiresAtRangeRule(80, 86400),
        ),
    )
}
