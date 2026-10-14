package com.acme.transport.lastmile.redelivery.overrideitem

/** A rule a redelivery override must pass. Check returns its problems, or none. */
interface RedeliveryOverrideRule {
    fun check(item: RedeliveryOverride): MutableList<String>
}

data class RedeliveryOverrideRequiredCodeRule(var message: String) : RedeliveryOverrideRule {
    override fun check(item: RedeliveryOverride): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RedeliveryOverrideTitleLengthRule(var maxLength: Int) : RedeliveryOverrideRule {
    override fun check(item: RedeliveryOverride): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RedeliveryOverrideCostRangeRule(var min: Double, var max: Double) : RedeliveryOverrideRule {
    override fun check(item: RedeliveryOverride): MutableList<String> {
        val value = item.cost
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("cost $value is below $min")
        }
        if (value > max) {
            problems.add("cost $value is above $max")
        }
        return problems
    }
}

data class RedeliveryOverrideRequiredTitleRule(var message: String) : RedeliveryOverrideRule {
    override fun check(item: RedeliveryOverride): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RedeliveryOverrideRequiredLabelRule(var message: String) : RedeliveryOverrideRule {
    override fun check(item: RedeliveryOverride): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a redelivery override. */
data class RedeliveryOverrideValidator(var rules: MutableList<RedeliveryOverrideRule>) {
    fun validate(item: RedeliveryOverride): MutableList<String> {
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

    fun requireValid(item: RedeliveryOverride) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RedeliveryOverride>): MutableMap<String, MutableList<String>> {
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

fun defaultRedeliveryOverrideValidator(): RedeliveryOverrideValidator {
    return RedeliveryOverrideValidator(
        mutableListOf<RedeliveryOverrideRule>(
            RedeliveryOverrideRequiredCodeRule("redelivery override needs a value"),
            RedeliveryOverrideTitleLengthRule(1000),
            RedeliveryOverrideCostRangeRule(7.4, 1432.0),
            RedeliveryOverrideRequiredTitleRule("redelivery override needs this field"),
            RedeliveryOverrideRequiredLabelRule("redelivery override needs this field"),
        ),
    )
}
