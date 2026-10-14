package com.acme.transport.customs.broker.agreement

/** A rule a broker agreement must pass. Check returns its problems, or none. */
interface BrokerAgreementRule {
    fun check(item: BrokerAgreement): MutableList<String>
}

data class BrokerAgreementRequiredCodeRule(var message: String) : BrokerAgreementRule {
    override fun check(item: BrokerAgreement): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BrokerAgreementRequiredCategoryRule(var message: String) : BrokerAgreementRule {
    override fun check(item: BrokerAgreement): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BrokerAgreementRequiredTrailerIdRule(var message: String) : BrokerAgreementRule {
    override fun check(item: BrokerAgreement): MutableList<String> {
        if (item.trailerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BrokerAgreementCodeLengthRule(var maxLength: Int) : BrokerAgreementRule {
    override fun check(item: BrokerAgreement): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BrokerAgreementOdometerRangeRule(var min: Int, var max: Int) : BrokerAgreementRule {
    override fun check(item: BrokerAgreement): MutableList<String> {
        val value = item.odometer
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("odometer $value is below $min")
        }
        if (value > max) {
            problems.add("odometer $value is above $max")
        }
        return problems
    }
}

data class BrokerAgreementScoreRangeRule(var min: Double, var max: Double) : BrokerAgreementRule {
    override fun check(item: BrokerAgreement): MutableList<String> {
        val value = item.score
        if (value < min || value > max) {
            return mutableListOf<String>("score $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a broker agreement. */
data class BrokerAgreementValidator(var rules: MutableList<BrokerAgreementRule>) {
    fun validate(item: BrokerAgreement): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BrokerAgreement) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BrokerAgreement>): MutableMap<String, MutableList<String>> {
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

fun defaultBrokerAgreementValidator(): BrokerAgreementValidator {
    return BrokerAgreementValidator(
        mutableListOf<BrokerAgreementRule>(
            BrokerAgreementRequiredCodeRule("broker agreement needs a value"),
            BrokerAgreementRequiredCategoryRule("broker agreement needs this field"),
            BrokerAgreementRequiredTrailerIdRule("broker agreement needs a value"),
            BrokerAgreementCodeLengthRule(1000),
            BrokerAgreementOdometerRangeRule(32, 64),
            BrokerAgreementScoreRangeRule(6.4, 2907.1),
        ),
    )
}
