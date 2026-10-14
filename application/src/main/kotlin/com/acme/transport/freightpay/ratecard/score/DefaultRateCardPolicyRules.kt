package com.acme.transport.freightpay.ratecard.score

/** A rule a default rate card policy must pass. Check returns its problems, or none. */
interface DefaultRateCardPolicyRule {
    fun check(item: DefaultRateCardPolicy): MutableList<String>
}

data class DefaultRateCardPolicyLabelLengthRule(var maxLength: Int) : DefaultRateCardPolicyRule {
    override fun check(item: DefaultRateCardPolicy): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultRateCardPolicyCodeLengthRule(var maxLength: Int) : DefaultRateCardPolicyRule {
    override fun check(item: DefaultRateCardPolicy): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultRateCardPolicyNotesLengthRule(var maxLength: Int) : DefaultRateCardPolicyRule {
    override fun check(item: DefaultRateCardPolicy): MutableList<String> {
        val size = item.notes.length
        if (size > maxLength) {
            return mutableListOf<String>("notes is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default rate card policy. */
data class DefaultRateCardPolicyValidator(var rules: MutableList<DefaultRateCardPolicyRule>) {
    fun validate(item: DefaultRateCardPolicy): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefaultRateCardPolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultRateCardPolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultRateCardPolicyValidator(): DefaultRateCardPolicyValidator {
    return DefaultRateCardPolicyValidator(
        mutableListOf<DefaultRateCardPolicyRule>(
            DefaultRateCardPolicyLabelLengthRule(86400),
            DefaultRateCardPolicyCodeLengthRule(32),
            DefaultRateCardPolicyNotesLengthRule(64),
        ),
    )
}
