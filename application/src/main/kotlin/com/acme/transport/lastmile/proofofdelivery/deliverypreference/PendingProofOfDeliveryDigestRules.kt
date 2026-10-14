package com.acme.transport.lastmile.proofofdelivery.deliverypreference

/** A rule a pending proof of delivery digest must pass. Check returns its problems, or none. */
interface PendingProofOfDeliveryDigestRule {
    fun check(item: PendingProofOfDeliveryDigest): MutableList<String>
}

data class PendingProofOfDeliveryDigestLabelLengthRule(var maxLength: Int) : PendingProofOfDeliveryDigestRule {
    override fun check(item: PendingProofOfDeliveryDigest): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingProofOfDeliveryDigestRequiredCodeRule(
    var message: String,
) : PendingProofOfDeliveryDigestRule {
    override fun check(item: PendingProofOfDeliveryDigest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingProofOfDeliveryDigestCodePrefixRule(var prefix: String) : PendingProofOfDeliveryDigestRule {
    override fun check(item: PendingProofOfDeliveryDigest): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class PendingProofOfDeliveryDigestNameLengthRule(var maxLength: Int) : PendingProofOfDeliveryDigestRule {
    override fun check(item: PendingProofOfDeliveryDigest): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingProofOfDeliveryDigestDurationMinutesRangeRule(
    var min: Int,
    var max: Int,
) : PendingProofOfDeliveryDigestRule {
    override fun check(item: PendingProofOfDeliveryDigest): MutableList<String> {
        val value = item.durationMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("duration minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending proof of delivery digest. */
data class PendingProofOfDeliveryDigestValidator(var rules: MutableList<PendingProofOfDeliveryDigestRule>) {
    fun validate(item: PendingProofOfDeliveryDigest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PendingProofOfDeliveryDigest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingProofOfDeliveryDigest>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingProofOfDeliveryDigestValidator(): PendingProofOfDeliveryDigestValidator {
    return PendingProofOfDeliveryDigestValidator(
        mutableListOf<PendingProofOfDeliveryDigestRule>(
            PendingProofOfDeliveryDigestLabelLengthRule(255),
            PendingProofOfDeliveryDigestRequiredCodeRule("pending proof of delivery digest needs an entry"),
            PendingProofOfDeliveryDigestCodePrefixRule("OPS"),
            PendingProofOfDeliveryDigestNameLengthRule(80),
            PendingProofOfDeliveryDigestDurationMinutesRangeRule(120, 86400),
        ),
    )
}
