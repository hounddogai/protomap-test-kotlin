package com.acme.transport.emissions.emissionfactor.factorpreference

/** A rule a rolling emission factor agreement must pass. Check returns its problems, or none. */
interface RollingEmissionFactorAgreementRule {
    fun check(item: RollingEmissionFactorAgreement): MutableList<String>
}

data class RollingEmissionFactorAgreementRequiredLabelRule(
    var message: String,
) : RollingEmissionFactorAgreementRule {
    override fun check(item: RollingEmissionFactorAgreement): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingEmissionFactorAgreementCodeLengthRule(
    var maxLength: Int,
) : RollingEmissionFactorAgreementRule {
    override fun check(item: RollingEmissionFactorAgreement): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingEmissionFactorAgreementLabelLengthRule(
    var maxLength: Int,
) : RollingEmissionFactorAgreementRule {
    override fun check(item: RollingEmissionFactorAgreement): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingEmissionFactorAgreementRequiredChannelRule(
    var message: String,
) : RollingEmissionFactorAgreementRule {
    override fun check(item: RollingEmissionFactorAgreement): MutableList<String> {
        if (item.channel.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling emission factor agreement. */
data class RollingEmissionFactorAgreementValidator(var rules: MutableList<RollingEmissionFactorAgreementRule>) {
    fun validate(item: RollingEmissionFactorAgreement): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RollingEmissionFactorAgreement) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingEmissionFactorAgreement>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingEmissionFactorAgreementValidator(): RollingEmissionFactorAgreementValidator {
    return RollingEmissionFactorAgreementValidator(
        mutableListOf<RollingEmissionFactorAgreementRule>(
            RollingEmissionFactorAgreementRequiredLabelRule("rolling emission factor agreement needs a value"),
            RollingEmissionFactorAgreementCodeLengthRule(64),
            RollingEmissionFactorAgreementLabelLengthRule(86400),
            RollingEmissionFactorAgreementRequiredChannelRule(
                "rolling emission factor agreement needs an entry",
            ),
        ),
    )
}
