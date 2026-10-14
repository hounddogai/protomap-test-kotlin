package com.acme.transport.lastmile.proofofdelivery.notice

/** A rule a adaptive proof of delivery record must pass. Check returns its problems, or none. */
interface AdaptiveProofOfDeliveryRecordRule {
    fun check(item: AdaptiveProofOfDeliveryRecord): MutableList<String>
}

data class AdaptiveProofOfDeliveryRecordRevisionRangeRule(
    var min: Int,
    var max: Int,
) : AdaptiveProofOfDeliveryRecordRule {
    override fun check(item: AdaptiveProofOfDeliveryRecord): MutableList<String> {
        val value = item.revision
        if (value < min || value > max) {
            return mutableListOf<String>("revision $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveProofOfDeliveryRecordRequiredTimezoneRule(
    var message: String,
) : AdaptiveProofOfDeliveryRecordRule {
    override fun check(item: AdaptiveProofOfDeliveryRecord): MutableList<String> {
        if (item.timezone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveProofOfDeliveryRecordTimezoneLengthRule(
    var maxLength: Int,
) : AdaptiveProofOfDeliveryRecordRule {
    override fun check(item: AdaptiveProofOfDeliveryRecord): MutableList<String> {
        val size = item.timezone.length
        if (size > maxLength) {
            return mutableListOf<String>("timezone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveProofOfDeliveryRecordRequiredCodeRule(
    var message: String,
) : AdaptiveProofOfDeliveryRecordRule {
    override fun check(item: AdaptiveProofOfDeliveryRecord): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveProofOfDeliveryRecordPhoneLengthRule(
    var maxLength: Int,
) : AdaptiveProofOfDeliveryRecordRule {
    override fun check(item: AdaptiveProofOfDeliveryRecord): MutableList<String> {
        val size = item.phone.length
        if (size > maxLength) {
            return mutableListOf<String>("phone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive proof of delivery record. */
data class AdaptiveProofOfDeliveryRecordValidator(var rules: MutableList<AdaptiveProofOfDeliveryRecordRule>) {
    fun validate(item: AdaptiveProofOfDeliveryRecord): MutableList<String> {
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

    fun requireValid(item: AdaptiveProofOfDeliveryRecord) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveProofOfDeliveryRecord>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveProofOfDeliveryRecordValidator(): AdaptiveProofOfDeliveryRecordValidator {
    return AdaptiveProofOfDeliveryRecordValidator(
        mutableListOf<AdaptiveProofOfDeliveryRecordRule>(
            AdaptiveProofOfDeliveryRecordRevisionRangeRule(1000, 86400),
            AdaptiveProofOfDeliveryRecordRequiredTimezoneRule(
                "adaptive proof of delivery record needs an entry",
            ),
            AdaptiveProofOfDeliveryRecordTimezoneLengthRule(80),
            AdaptiveProofOfDeliveryRecordRequiredCodeRule("adaptive proof of delivery record needs an entry"),
            AdaptiveProofOfDeliveryRecordPhoneLengthRule(86400),
        ),
    )
}
