package com.acme.transport.network.zone.plan

/** A rule a tiered zone plan must pass. Check returns its problems, or none. */
interface TieredZonePlanRule {
    fun check(item: TieredZonePlan): MutableList<String>
}

data class TieredZonePlanTrailerIdLengthRule(var maxLength: Int) : TieredZonePlanRule {
    override fun check(item: TieredZonePlan): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredZonePlanAmountRangeRule(var min: Double, var max: Double) : TieredZonePlanRule {
    override fun check(item: TieredZonePlan): MutableList<String> {
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

data class TieredZonePlanGraceDaysRangeRule(var min: Int, var max: Int) : TieredZonePlanRule {
    override fun check(item: TieredZonePlan): MutableList<String> {
        val value = item.graceDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("grace days $value is below $min")
        }
        if (value > max) {
            problems.add("grace days $value is above $max")
        }
        return problems
    }
}

data class TieredZonePlanRequiredTitleRule(var message: String) : TieredZonePlanRule {
    override fun check(item: TieredZonePlan): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered zone plan. */
data class TieredZonePlanValidator(var rules: MutableList<TieredZonePlanRule>) {
    fun validate(item: TieredZonePlan): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TieredZonePlan) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredZonePlan>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredZonePlanValidator(): TieredZonePlanValidator {
    return TieredZonePlanValidator(
        mutableListOf<TieredZonePlanRule>(
            TieredZonePlanTrailerIdLengthRule(64),
            TieredZonePlanAmountRangeRule(7.1, 2970.1),
            TieredZonePlanGraceDaysRangeRule(64, 255),
            TieredZonePlanRequiredTitleRule("tiered zone plan needs an entry"),
        ),
    )
}
