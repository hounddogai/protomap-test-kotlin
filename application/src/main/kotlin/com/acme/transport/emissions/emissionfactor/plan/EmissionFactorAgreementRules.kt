package com.acme.transport.emissions.emissionfactor.plan

/** A rule a emission factor agreement must pass. Check returns its problems, or none. */
interface EmissionFactorAgreementRule {
    fun check(item: EmissionFactorAgreement): MutableList<String>
}

data class EmissionFactorAgreementQuantityRangeRule(var min: Int, var max: Int) : EmissionFactorAgreementRule {
    override fun check(item: EmissionFactorAgreement): MutableList<String> {
        val value = item.quantity
        if (value < min || value > max) {
            return mutableListOf<String>("quantity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class EmissionFactorAgreementThresholdRangeRule(
    var min: Double,
    var max: Double,
) : EmissionFactorAgreementRule {
    override fun check(item: EmissionFactorAgreement): MutableList<String> {
        val value = item.threshold
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("threshold $value is below $min")
        }
        if (value > max) {
            problems.add("threshold $value is above $max")
        }
        return problems
    }
}

data class EmissionFactorAgreementOwnerIdLengthRule(var maxLength: Int) : EmissionFactorAgreementRule {
    override fun check(item: EmissionFactorAgreement): MutableList<String> {
        val size = item.ownerId.length
        if (size > maxLength) {
            return mutableListOf<String>("owner id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class EmissionFactorAgreementRequiredOwnerIdRule(var message: String) : EmissionFactorAgreementRule {
    override fun check(item: EmissionFactorAgreement): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class EmissionFactorAgreementRequiredTitleRule(var message: String) : EmissionFactorAgreementRule {
    override fun check(item: EmissionFactorAgreement): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a emission factor agreement. */
data class EmissionFactorAgreementValidator(var rules: MutableList<EmissionFactorAgreementRule>) {
    fun validate(item: EmissionFactorAgreement): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: EmissionFactorAgreement) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<EmissionFactorAgreement>): MutableMap<String, MutableList<String>> {
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

fun defaultEmissionFactorAgreementValidator(): EmissionFactorAgreementValidator {
    return EmissionFactorAgreementValidator(
        mutableListOf<EmissionFactorAgreementRule>(
            EmissionFactorAgreementQuantityRangeRule(120, 120),
            EmissionFactorAgreementThresholdRangeRule(4.8, 3381.2),
            EmissionFactorAgreementOwnerIdLengthRule(255),
            EmissionFactorAgreementRequiredOwnerIdRule("emission factor agreement needs an entry"),
            EmissionFactorAgreementRequiredTitleRule("emission factor agreement needs a value"),
        ),
    )
}
