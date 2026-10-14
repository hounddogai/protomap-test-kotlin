package com.acme.transport.lastmile.proofofdelivery.deliveryrollup

/** A rule a rolling proof of delivery quota must pass. Check returns its problems, or none. */
interface RollingProofOfDeliveryQuotaRule {
    fun check(item: RollingProofOfDeliveryQuota): MutableList<String>
}

data class RollingProofOfDeliveryQuotaDriverIdLengthRule(var maxLength: Int) : RollingProofOfDeliveryQuotaRule {
    override fun check(item: RollingProofOfDeliveryQuota): MutableList<String> {
        val size = item.driverId.length
        if (size > maxLength) {
            return mutableListOf<String>("driver id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingProofOfDeliveryQuotaAttemptsRangeRule(
    var min: Int,
    var max: Int,
) : RollingProofOfDeliveryQuotaRule {
    override fun check(item: RollingProofOfDeliveryQuota): MutableList<String> {
        val value = item.attempts
        if (value < min || value > max) {
            return mutableListOf<String>("attempts $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RollingProofOfDeliveryQuotaRequiredCodeRule(var message: String) : RollingProofOfDeliveryQuotaRule {
    override fun check(item: RollingProofOfDeliveryQuota): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingProofOfDeliveryQuotaCodeLengthRule(var maxLength: Int) : RollingProofOfDeliveryQuotaRule {
    override fun check(item: RollingProofOfDeliveryQuota): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingProofOfDeliveryQuotaCodePrefixRule(var prefix: String) : RollingProofOfDeliveryQuotaRule {
    override fun check(item: RollingProofOfDeliveryQuota): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RollingProofOfDeliveryQuotaRateRangeRule(
    var min: Double,
    var max: Double,
) : RollingProofOfDeliveryQuotaRule {
    override fun check(item: RollingProofOfDeliveryQuota): MutableList<String> {
        val value = item.rate
        if (value < min || value > max) {
            return mutableListOf<String>("rate $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling proof of delivery quota. */
data class RollingProofOfDeliveryQuotaValidator(var rules: MutableList<RollingProofOfDeliveryQuotaRule>) {
    fun validate(item: RollingProofOfDeliveryQuota): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RollingProofOfDeliveryQuota) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingProofOfDeliveryQuota>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingProofOfDeliveryQuotaValidator(): RollingProofOfDeliveryQuotaValidator {
    return RollingProofOfDeliveryQuotaValidator(
        mutableListOf<RollingProofOfDeliveryQuotaRule>(
            RollingProofOfDeliveryQuotaDriverIdLengthRule(80),
            RollingProofOfDeliveryQuotaAttemptsRangeRule(32, 86400),
            RollingProofOfDeliveryQuotaRequiredCodeRule("rolling proof of delivery quota needs a value"),
            RollingProofOfDeliveryQuotaCodeLengthRule(120),
            RollingProofOfDeliveryQuotaCodePrefixRule("ACM"),
            RollingProofOfDeliveryQuotaRateRangeRule(5.7, 2027.9),
        ),
    )
}
