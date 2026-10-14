package com.acme.transport.freightpay.fuelsurcharge.surchargerule

/** A rule a tiered fuel surcharge rule must pass. Check returns its problems, or none. */
interface TieredFuelSurchargeRuleRule {
    fun check(item: TieredFuelSurchargeRule): MutableList<String>
}

data class TieredFuelSurchargeRuleRequiredCodeRule(var message: String) : TieredFuelSurchargeRuleRule {
    override fun check(item: TieredFuelSurchargeRule): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredFuelSurchargeRuleRequiredChannelRule(var message: String) : TieredFuelSurchargeRuleRule {
    override fun check(item: TieredFuelSurchargeRule): MutableList<String> {
        if (item.channel.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredFuelSurchargeRuleChannelLengthRule(var maxLength: Int) : TieredFuelSurchargeRuleRule {
    override fun check(item: TieredFuelSurchargeRule): MutableList<String> {
        val size = item.channel.length
        if (size > maxLength) {
            return mutableListOf<String>("channel is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredFuelSurchargeRuleCodePrefixRule(var prefix: String) : TieredFuelSurchargeRuleRule {
    override fun check(item: TieredFuelSurchargeRule): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TieredFuelSurchargeRuleSummaryLengthRule(var maxLength: Int) : TieredFuelSurchargeRuleRule {
    override fun check(item: TieredFuelSurchargeRule): MutableList<String> {
        val size = item.summary.length
        if (size > maxLength) {
            return mutableListOf<String>("summary is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredFuelSurchargeRuleRequiredTitleRule(var message: String) : TieredFuelSurchargeRuleRule {
    override fun check(item: TieredFuelSurchargeRule): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered fuel surcharge rule. */
data class TieredFuelSurchargeRuleValidator(var rules: MutableList<TieredFuelSurchargeRuleRule>) {
    fun validate(item: TieredFuelSurchargeRule): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TieredFuelSurchargeRule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredFuelSurchargeRule>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredFuelSurchargeRuleValidator(): TieredFuelSurchargeRuleValidator {
    return TieredFuelSurchargeRuleValidator(
        mutableListOf<TieredFuelSurchargeRuleRule>(
            TieredFuelSurchargeRuleRequiredCodeRule("tiered fuel surcharge rule needs this field"),
            TieredFuelSurchargeRuleRequiredChannelRule("tiered fuel surcharge rule needs this field"),
            TieredFuelSurchargeRuleChannelLengthRule(32),
            TieredFuelSurchargeRuleCodePrefixRule("Q"),
            TieredFuelSurchargeRuleSummaryLengthRule(3600),
            TieredFuelSurchargeRuleRequiredTitleRule("tiered fuel surcharge rule needs this field"),
        ),
    )
}
