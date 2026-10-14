package com.acme.transport.customs.broker.brokerslot

/** A rule a global broker agreement must pass. Check returns its problems, or none. */
interface GlobalBrokerAgreementRule {
    fun check(item: GlobalBrokerAgreement): MutableList<String>
}

data class GlobalBrokerAgreementCodeLengthRule(var maxLength: Int) : GlobalBrokerAgreementRule {
    override fun check(item: GlobalBrokerAgreement): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalBrokerAgreementRequiredLabelRule(var message: String) : GlobalBrokerAgreementRule {
    override fun check(item: GlobalBrokerAgreement): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalBrokerAgreementCodePrefixRule(var prefix: String) : GlobalBrokerAgreementRule {
    override fun check(item: GlobalBrokerAgreement): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class GlobalBrokerAgreementSourceLengthRule(var maxLength: Int) : GlobalBrokerAgreementRule {
    override fun check(item: GlobalBrokerAgreement): MutableList<String> {
        val size = item.source.length
        if (size > maxLength) {
            return mutableListOf<String>("source is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalBrokerAgreementLocaleLengthRule(var maxLength: Int) : GlobalBrokerAgreementRule {
    override fun check(item: GlobalBrokerAgreement): MutableList<String> {
        val size = item.locale.length
        if (size > maxLength) {
            return mutableListOf<String>("locale is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalBrokerAgreementDurationMinutesRangeRule(
    var min: Int,
    var max: Int,
) : GlobalBrokerAgreementRule {
    override fun check(item: GlobalBrokerAgreement): MutableList<String> {
        val value = item.durationMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("duration minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global broker agreement. */
data class GlobalBrokerAgreementValidator(var rules: MutableList<GlobalBrokerAgreementRule>) {
    fun validate(item: GlobalBrokerAgreement): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: GlobalBrokerAgreement) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalBrokerAgreement>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalBrokerAgreementValidator(): GlobalBrokerAgreementValidator {
    return GlobalBrokerAgreementValidator(
        mutableListOf<GlobalBrokerAgreementRule>(
            GlobalBrokerAgreementCodeLengthRule(3600),
            GlobalBrokerAgreementRequiredLabelRule("global broker agreement needs an entry"),
            GlobalBrokerAgreementCodePrefixRule("Q"),
            GlobalBrokerAgreementSourceLengthRule(1000),
            GlobalBrokerAgreementLocaleLengthRule(3600),
            GlobalBrokerAgreementDurationMinutesRangeRule(80, 3600),
        ),
    )
}
