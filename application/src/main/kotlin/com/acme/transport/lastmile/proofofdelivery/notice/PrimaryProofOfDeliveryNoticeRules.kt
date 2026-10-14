package com.acme.transport.lastmile.proofofdelivery.notice

/** A rule a primary proof of delivery notice must pass. Check returns its problems, or none. */
interface PrimaryProofOfDeliveryNoticeRule {
    fun check(item: PrimaryProofOfDeliveryNotice): MutableList<String>
}

data class PrimaryProofOfDeliveryNoticeVehicleIdLengthRule(
    var maxLength: Int,
) : PrimaryProofOfDeliveryNoticeRule {
    override fun check(item: PrimaryProofOfDeliveryNotice): MutableList<String> {
        val size = item.vehicleId.length
        if (size > maxLength) {
            return mutableListOf<String>("vehicle id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryProofOfDeliveryNoticeCapacityRangeRule(
    var min: Int,
    var max: Int,
) : PrimaryProofOfDeliveryNoticeRule {
    override fun check(item: PrimaryProofOfDeliveryNotice): MutableList<String> {
        val value = item.capacity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("capacity $value is below $min")
        }
        if (value > max) {
            problems.add("capacity $value is above $max")
        }
        return problems
    }
}

data class PrimaryProofOfDeliveryNoticeMaximumRangeRule(
    var min: Double,
    var max: Double,
) : PrimaryProofOfDeliveryNoticeRule {
    override fun check(item: PrimaryProofOfDeliveryNotice): MutableList<String> {
        val value = item.maximum
        if (value < min || value > max) {
            return mutableListOf<String>("maximum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryProofOfDeliveryNoticeCodePrefixRule(var prefix: String) : PrimaryProofOfDeliveryNoticeRule {
    override fun check(item: PrimaryProofOfDeliveryNotice): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary proof of delivery notice. */
data class PrimaryProofOfDeliveryNoticeValidator(var rules: MutableList<PrimaryProofOfDeliveryNoticeRule>) {
    fun validate(item: PrimaryProofOfDeliveryNotice): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PrimaryProofOfDeliveryNotice) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryProofOfDeliveryNotice>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryProofOfDeliveryNoticeValidator(): PrimaryProofOfDeliveryNoticeValidator {
    return PrimaryProofOfDeliveryNoticeValidator(
        mutableListOf<PrimaryProofOfDeliveryNoticeRule>(
            PrimaryProofOfDeliveryNoticeVehicleIdLengthRule(255),
            PrimaryProofOfDeliveryNoticeCapacityRangeRule(120, 255),
            PrimaryProofOfDeliveryNoticeMaximumRangeRule(1.3, 2102.8),
            PrimaryProofOfDeliveryNoticeCodePrefixRule("X"),
        ),
    )
}
