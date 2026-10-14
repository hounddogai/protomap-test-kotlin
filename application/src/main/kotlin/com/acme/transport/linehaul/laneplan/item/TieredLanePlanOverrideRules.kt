package com.acme.transport.linehaul.laneplan.item

/** A rule a tiered lane plan override must pass. Check returns its problems, or none. */
interface TieredLanePlanOverrideRule {
    fun check(item: TieredLanePlanOverride): MutableList<String>
}

data class TieredLanePlanOverrideRequiredLabelRule(var message: String) : TieredLanePlanOverrideRule {
    override fun check(item: TieredLanePlanOverride): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredLanePlanOverrideCurrencyLengthRule(var maxLength: Int) : TieredLanePlanOverrideRule {
    override fun check(item: TieredLanePlanOverride): MutableList<String> {
        val size = item.currency.length
        if (size > maxLength) {
            return mutableListOf<String>("currency is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredLanePlanOverrideRequiredCodeRule(var message: String) : TieredLanePlanOverrideRule {
    override fun check(item: TieredLanePlanOverride): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredLanePlanOverrideRequiredCurrencyRule(var message: String) : TieredLanePlanOverrideRule {
    override fun check(item: TieredLanePlanOverride): MutableList<String> {
        if (item.currency.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredLanePlanOverrideSequenceRangeRule(var min: Int, var max: Int) : TieredLanePlanOverrideRule {
    override fun check(item: TieredLanePlanOverride): MutableList<String> {
        val value = item.sequence
        if (value < min || value > max) {
            return mutableListOf<String>("sequence $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered lane plan override. */
data class TieredLanePlanOverrideValidator(var rules: MutableList<TieredLanePlanOverrideRule>) {
    fun validate(item: TieredLanePlanOverride): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TieredLanePlanOverride) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredLanePlanOverride>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredLanePlanOverrideValidator(): TieredLanePlanOverrideValidator {
    return TieredLanePlanOverrideValidator(
        mutableListOf<TieredLanePlanOverrideRule>(
            TieredLanePlanOverrideRequiredLabelRule("tiered lane plan override needs a value"),
            TieredLanePlanOverrideCurrencyLengthRule(32),
            TieredLanePlanOverrideRequiredCodeRule("tiered lane plan override needs an entry"),
            TieredLanePlanOverrideRequiredCurrencyRule("tiered lane plan override needs an entry"),
            TieredLanePlanOverrideSequenceRangeRule(32, 80),
        ),
    )
}
