package com.acme.transport.emissions.emissionfactor.agreement

/** A rule a fallback emission factor agreement must pass. Check returns its problems, or none. */
interface FallbackEmissionFactorAgreementRule {
    fun check(item: FallbackEmissionFactorAgreement): MutableList<String>
}

data class FallbackEmissionFactorAgreementCodePrefixRule(
    var prefix: String,
) : FallbackEmissionFactorAgreementRule {
    override fun check(item: FallbackEmissionFactorAgreement): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class FallbackEmissionFactorAgreementActiveGuardRule(
    var limit: Double,
) : FallbackEmissionFactorAgreementRule {
    override fun check(item: FallbackEmissionFactorAgreement): MutableList<String> {
        if (item.active && item.minimum > limit) {
            return mutableListOf<String>("minimum ${item.minimum} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

data class FallbackEmissionFactorAgreementMinimumRangeRule(
    var min: Double,
    var max: Double,
) : FallbackEmissionFactorAgreementRule {
    override fun check(item: FallbackEmissionFactorAgreement): MutableList<String> {
        val value = item.minimum
        if (value < min || value > max) {
            return mutableListOf<String>("minimum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FallbackEmissionFactorAgreementTitleLengthRule(
    var maxLength: Int,
) : FallbackEmissionFactorAgreementRule {
    override fun check(item: FallbackEmissionFactorAgreement): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackEmissionFactorAgreementRequiredCodeRule(
    var message: String,
) : FallbackEmissionFactorAgreementRule {
    override fun check(item: FallbackEmissionFactorAgreement): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback emission factor agreement. */
data class FallbackEmissionFactorAgreementValidator(
    var rules: MutableList<FallbackEmissionFactorAgreementRule>,
) {
    fun validate(item: FallbackEmissionFactorAgreement): MutableList<String> {
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

    fun requireValid(item: FallbackEmissionFactorAgreement) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackEmissionFactorAgreement>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackEmissionFactorAgreementValidator(): FallbackEmissionFactorAgreementValidator {
    return FallbackEmissionFactorAgreementValidator(
        mutableListOf<FallbackEmissionFactorAgreementRule>(
            FallbackEmissionFactorAgreementCodePrefixRule("ACM"),
            FallbackEmissionFactorAgreementActiveGuardRule(4.0),
            FallbackEmissionFactorAgreementMinimumRangeRule(7.9, 181.0),
            FallbackEmissionFactorAgreementTitleLengthRule(80),
            FallbackEmissionFactorAgreementRequiredCodeRule("fallback emission factor agreement needs a value"),
        ),
    )
}
