package com.acme.transport.lastmile.proofofdelivery.deliveryrecord

/** A rule a primary proof of delivery record must pass. Check returns its problems, or none. */
interface PrimaryProofOfDeliveryRecordRule {
    fun check(item: PrimaryProofOfDeliveryRecord): MutableList<String>
}

data class PrimaryProofOfDeliveryRecordLoadKgRangeRule(
    var min: Double,
    var max: Double,
) : PrimaryProofOfDeliveryRecordRule {
    override fun check(item: PrimaryProofOfDeliveryRecord): MutableList<String> {
        val value = item.loadKg
        if (value < min || value > max) {
            return mutableListOf<String>("load kg $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryProofOfDeliveryRecordPhoneLengthRule(var maxLength: Int) : PrimaryProofOfDeliveryRecordRule {
    override fun check(item: PrimaryProofOfDeliveryRecord): MutableList<String> {
        val size = item.phone.length
        if (size > maxLength) {
            return mutableListOf<String>("phone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryProofOfDeliveryRecordSourceLengthRule(var maxLength: Int) : PrimaryProofOfDeliveryRecordRule {
    override fun check(item: PrimaryProofOfDeliveryRecord): MutableList<String> {
        val size = item.source.length
        if (size > maxLength) {
            return mutableListOf<String>("source is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryProofOfDeliveryRecordRequiredPhoneRule(
    var message: String,
) : PrimaryProofOfDeliveryRecordRule {
    override fun check(item: PrimaryProofOfDeliveryRecord): MutableList<String> {
        if (item.phone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary proof of delivery record. */
data class PrimaryProofOfDeliveryRecordValidator(var rules: MutableList<PrimaryProofOfDeliveryRecordRule>) {
    fun validate(item: PrimaryProofOfDeliveryRecord): MutableList<String> {
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

    fun requireValid(item: PrimaryProofOfDeliveryRecord) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryProofOfDeliveryRecord>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryProofOfDeliveryRecordValidator(): PrimaryProofOfDeliveryRecordValidator {
    return PrimaryProofOfDeliveryRecordValidator(
        mutableListOf<PrimaryProofOfDeliveryRecordRule>(
            PrimaryProofOfDeliveryRecordLoadKgRangeRule(2.7, 4890.4),
            PrimaryProofOfDeliveryRecordPhoneLengthRule(255),
            PrimaryProofOfDeliveryRecordSourceLengthRule(64),
            PrimaryProofOfDeliveryRecordRequiredPhoneRule("primary proof of delivery record needs a value"),
        ),
    )
}
