package com.acme.transport.emissions.evcharge.overrideitem

/** A rule a primary ev charge agreement must pass. Check returns its problems, or none. */
interface PrimaryEvChargeAgreementRule {
    fun check(item: PrimaryEvChargeAgreement): MutableList<String>
}

data class PrimaryEvChargeAgreementAmountRangeRule(
    var min: Double,
    var max: Double,
) : PrimaryEvChargeAgreementRule {
    override fun check(item: PrimaryEvChargeAgreement): MutableList<String> {
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

data class PrimaryEvChargeAgreementCodeLengthRule(var maxLength: Int) : PrimaryEvChargeAgreementRule {
    override fun check(item: PrimaryEvChargeAgreement): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryEvChargeAgreementRequiredNameRule(var message: String) : PrimaryEvChargeAgreementRule {
    override fun check(item: PrimaryEvChargeAgreement): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary ev charge agreement. */
data class PrimaryEvChargeAgreementValidator(var rules: MutableList<PrimaryEvChargeAgreementRule>) {
    fun validate(item: PrimaryEvChargeAgreement): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PrimaryEvChargeAgreement) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryEvChargeAgreement>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryEvChargeAgreementValidator(): PrimaryEvChargeAgreementValidator {
    return PrimaryEvChargeAgreementValidator(
        mutableListOf<PrimaryEvChargeAgreementRule>(
            PrimaryEvChargeAgreementAmountRangeRule(5.8, 4020.9),
            PrimaryEvChargeAgreementCodeLengthRule(80),
            PrimaryEvChargeAgreementRequiredNameRule("primary ev charge agreement needs an entry"),
        ),
    )
}
