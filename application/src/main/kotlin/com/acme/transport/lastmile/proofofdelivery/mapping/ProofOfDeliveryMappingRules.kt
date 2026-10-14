package com.acme.transport.lastmile.proofofdelivery.mapping

/** A rule a proof of delivery mapping must pass. Check returns its problems, or none. */
interface ProofOfDeliveryMappingRule {
    fun check(item: ProofOfDeliveryMapping): MutableList<String>
}

data class ProofOfDeliveryMappingRequiredLocaleRule(var message: String) : ProofOfDeliveryMappingRule {
    override fun check(item: ProofOfDeliveryMapping): MutableList<String> {
        if (item.locale.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ProofOfDeliveryMappingRequiredCurrencyRule(var message: String) : ProofOfDeliveryMappingRule {
    override fun check(item: ProofOfDeliveryMapping): MutableList<String> {
        if (item.currency.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ProofOfDeliveryMappingUpdatedAtRangeRule(var min: Int, var max: Int) : ProofOfDeliveryMappingRule {
    override fun check(item: ProofOfDeliveryMapping): MutableList<String> {
        val value = item.updatedAt
        if (value < min || value > max) {
            return mutableListOf<String>("updated at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ProofOfDeliveryMappingCurrencyLengthRule(var maxLength: Int) : ProofOfDeliveryMappingRule {
    override fun check(item: ProofOfDeliveryMapping): MutableList<String> {
        val size = item.currency.length
        if (size > maxLength) {
            return mutableListOf<String>("currency is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ProofOfDeliveryMappingAmountRangeRule(
    var min: Double,
    var max: Double,
) : ProofOfDeliveryMappingRule {
    override fun check(item: ProofOfDeliveryMapping): MutableList<String> {
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

data class ProofOfDeliveryMappingLocaleLengthRule(var maxLength: Int) : ProofOfDeliveryMappingRule {
    override fun check(item: ProofOfDeliveryMapping): MutableList<String> {
        val size = item.locale.length
        if (size > maxLength) {
            return mutableListOf<String>("locale is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a proof of delivery mapping. */
data class ProofOfDeliveryMappingValidator(var rules: MutableList<ProofOfDeliveryMappingRule>) {
    fun validate(item: ProofOfDeliveryMapping): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ProofOfDeliveryMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ProofOfDeliveryMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultProofOfDeliveryMappingValidator(): ProofOfDeliveryMappingValidator {
    return ProofOfDeliveryMappingValidator(
        mutableListOf<ProofOfDeliveryMappingRule>(
            ProofOfDeliveryMappingRequiredLocaleRule("proof of delivery mapping needs an entry"),
            ProofOfDeliveryMappingRequiredCurrencyRule("proof of delivery mapping needs a value"),
            ProofOfDeliveryMappingUpdatedAtRangeRule(3600, 86400),
            ProofOfDeliveryMappingCurrencyLengthRule(64),
            ProofOfDeliveryMappingAmountRangeRule(5.9, 2019.0),
            ProofOfDeliveryMappingLocaleLengthRule(3600),
        ),
    )
}
