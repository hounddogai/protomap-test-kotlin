package com.acme.transport.customs.broker.brokerslot

/** A rule a lenient broker agreement must pass. Check returns its problems, or none. */
interface LenientBrokerAgreementRule {
    fun check(item: LenientBrokerAgreement): MutableList<String>
}

data class LenientBrokerAgreementRequiredTitleRule(var message: String) : LenientBrokerAgreementRule {
    override fun check(item: LenientBrokerAgreement): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientBrokerAgreementPhoneLengthRule(var maxLength: Int) : LenientBrokerAgreementRule {
    override fun check(item: LenientBrokerAgreement): MutableList<String> {
        val size = item.phone.length
        if (size > maxLength) {
            return mutableListOf<String>("phone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientBrokerAgreementTitleLengthRule(var maxLength: Int) : LenientBrokerAgreementRule {
    override fun check(item: LenientBrokerAgreement): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientBrokerAgreementLoadKgRangeRule(
    var min: Double,
    var max: Double,
) : LenientBrokerAgreementRule {
    override fun check(item: LenientBrokerAgreement): MutableList<String> {
        val value = item.loadKg
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("load kg $value is below $min")
        }
        if (value > max) {
            problems.add("load kg $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a lenient broker agreement. */
data class LenientBrokerAgreementValidator(var rules: MutableList<LenientBrokerAgreementRule>) {
    fun validate(item: LenientBrokerAgreement): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LenientBrokerAgreement) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientBrokerAgreement>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientBrokerAgreementValidator(): LenientBrokerAgreementValidator {
    return LenientBrokerAgreementValidator(
        mutableListOf<LenientBrokerAgreementRule>(
            LenientBrokerAgreementRequiredTitleRule("lenient broker agreement needs this field"),
            LenientBrokerAgreementPhoneLengthRule(64),
            LenientBrokerAgreementTitleLengthRule(86400),
            LenientBrokerAgreementLoadKgRangeRule(5.8, 2986.0),
        ),
    )
}
