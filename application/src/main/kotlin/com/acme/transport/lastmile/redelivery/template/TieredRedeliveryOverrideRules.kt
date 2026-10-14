package com.acme.transport.lastmile.redelivery.template

import com.acme.transport.kit.text.normalize

/** A rule a tiered redelivery override must pass. Check returns its problems, or none. */
interface TieredRedeliveryOverrideRule {
    fun check(item: TieredRedeliveryOverride): MutableList<String>
}

data class TieredRedeliveryOverrideWeightRangeRule(
    var min: Double,
    var max: Double,
) : TieredRedeliveryOverrideRule {
    override fun check(item: TieredRedeliveryOverride): MutableList<String> {
        val value = item.weight
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("weight $value is below $min")
        }
        if (value > max) {
            problems.add("weight $value is above $max")
        }
        return problems
    }
}

data class TieredRedeliveryOverrideAllowedNameRule(
    var allowed: MutableList<String>,
) : TieredRedeliveryOverrideRule {
    override fun check(item: TieredRedeliveryOverride): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is too long")
        }
        return mutableListOf<String>()
    }
}

data class TieredRedeliveryOverrideRequiredNameRule(var message: String) : TieredRedeliveryOverrideRule {
    override fun check(item: TieredRedeliveryOverride): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered redelivery override. */
data class TieredRedeliveryOverrideValidator(var rules: MutableList<TieredRedeliveryOverrideRule>) {
    fun validate(item: TieredRedeliveryOverride): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TieredRedeliveryOverride) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredRedeliveryOverride>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredRedeliveryOverrideValidator(): TieredRedeliveryOverrideValidator {
    return TieredRedeliveryOverrideValidator(
        mutableListOf<TieredRedeliveryOverrideRule>(
            TieredRedeliveryOverrideWeightRangeRule(3.8, 1567.7),
            TieredRedeliveryOverrideAllowedNameRule(mutableListOf<String>("tiered")),
            TieredRedeliveryOverrideRequiredNameRule("tiered redelivery override needs a value"),
        ),
    )
}
