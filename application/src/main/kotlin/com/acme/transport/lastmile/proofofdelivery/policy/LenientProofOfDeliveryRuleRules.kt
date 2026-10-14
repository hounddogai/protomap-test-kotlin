package com.acme.transport.lastmile.proofofdelivery.policy

/** A rule a lenient proof of delivery rule must pass. Check returns its problems, or none. */
interface LenientProofOfDeliveryRuleRule {
    fun check(item: LenientProofOfDeliveryRule): MutableList<String>
}

data class LenientProofOfDeliveryRuleRequiredTimezoneRule(
    var message: String,
) : LenientProofOfDeliveryRuleRule {
    override fun check(item: LenientProofOfDeliveryRule): MutableList<String> {
        if (item.timezone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientProofOfDeliveryRuleLabelLengthRule(var maxLength: Int) : LenientProofOfDeliveryRuleRule {
    override fun check(item: LenientProofOfDeliveryRule): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientProofOfDeliveryRuleRequiredCodeRule(var message: String) : LenientProofOfDeliveryRuleRule {
    override fun check(item: LenientProofOfDeliveryRule): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient proof of delivery rule. */
data class LenientProofOfDeliveryRuleValidator(var rules: MutableList<LenientProofOfDeliveryRuleRule>) {
    fun validate(item: LenientProofOfDeliveryRule): MutableList<String> {
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

    fun requireValid(item: LenientProofOfDeliveryRule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientProofOfDeliveryRule>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientProofOfDeliveryRuleValidator(): LenientProofOfDeliveryRuleValidator {
    return LenientProofOfDeliveryRuleValidator(
        mutableListOf<LenientProofOfDeliveryRuleRule>(
            LenientProofOfDeliveryRuleRequiredTimezoneRule("lenient proof of delivery rule needs an entry"),
            LenientProofOfDeliveryRuleLabelLengthRule(3600),
            LenientProofOfDeliveryRuleRequiredCodeRule("lenient proof of delivery rule needs this field"),
        ),
    )
}
