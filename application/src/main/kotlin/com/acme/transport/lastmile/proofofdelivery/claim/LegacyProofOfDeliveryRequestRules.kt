package com.acme.transport.lastmile.proofofdelivery.claim

/** A rule a legacy proof of delivery request must pass. Check returns its problems, or none. */
interface LegacyProofOfDeliveryRequestRule {
    fun check(item: LegacyProofOfDeliveryRequest): MutableList<String>
}

data class LegacyProofOfDeliveryRequestCodePrefixRule(var prefix: String) : LegacyProofOfDeliveryRequestRule {
    override fun check(item: LegacyProofOfDeliveryRequest): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LegacyProofOfDeliveryRequestCodeLengthRule(var maxLength: Int) : LegacyProofOfDeliveryRequestRule {
    override fun check(item: LegacyProofOfDeliveryRequest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyProofOfDeliveryRequestUpdatedAtRangeRule(
    var min: Int,
    var max: Int,
) : LegacyProofOfDeliveryRequestRule {
    override fun check(item: LegacyProofOfDeliveryRequest): MutableList<String> {
        val value = item.updatedAt
        if (value < min || value > max) {
            return mutableListOf<String>("updated at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LegacyProofOfDeliveryRequestTitleLengthRule(var maxLength: Int) : LegacyProofOfDeliveryRequestRule {
    override fun check(item: LegacyProofOfDeliveryRequest): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyProofOfDeliveryRequestRequiredCodeRule(
    var message: String,
) : LegacyProofOfDeliveryRequestRule {
    override fun check(item: LegacyProofOfDeliveryRequest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyProofOfDeliveryRequestRequiredTimezoneRule(
    var message: String,
) : LegacyProofOfDeliveryRequestRule {
    override fun check(item: LegacyProofOfDeliveryRequest): MutableList<String> {
        if (item.timezone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy proof of delivery request. */
data class LegacyProofOfDeliveryRequestValidator(var rules: MutableList<LegacyProofOfDeliveryRequestRule>) {
    fun validate(item: LegacyProofOfDeliveryRequest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LegacyProofOfDeliveryRequest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyProofOfDeliveryRequest>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyProofOfDeliveryRequestValidator(): LegacyProofOfDeliveryRequestValidator {
    return LegacyProofOfDeliveryRequestValidator(
        mutableListOf<LegacyProofOfDeliveryRequestRule>(
            LegacyProofOfDeliveryRequestCodePrefixRule("Q"),
            LegacyProofOfDeliveryRequestCodeLengthRule(32),
            LegacyProofOfDeliveryRequestUpdatedAtRangeRule(32, 86400),
            LegacyProofOfDeliveryRequestTitleLengthRule(32),
            LegacyProofOfDeliveryRequestRequiredCodeRule("legacy proof of delivery request needs an entry"),
            LegacyProofOfDeliveryRequestRequiredTimezoneRule("legacy proof of delivery request needs an entry"),
        ),
    )
}
