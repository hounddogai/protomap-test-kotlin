package com.acme.transport.lastmile.proofofdelivery.estimate

/** A rule a adaptive proof of delivery schedule must pass. Check returns its problems, or none. */
interface AdaptiveProofOfDeliveryScheduleRule {
    fun check(item: AdaptiveProofOfDeliverySchedule): MutableList<String>
}

data class AdaptiveProofOfDeliveryScheduleRequiredSummaryRule(
    var message: String,
) : AdaptiveProofOfDeliveryScheduleRule {
    override fun check(item: AdaptiveProofOfDeliverySchedule): MutableList<String> {
        if (item.summary.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveProofOfDeliveryScheduleChecksumLengthRule(
    var maxLength: Int,
) : AdaptiveProofOfDeliveryScheduleRule {
    override fun check(item: AdaptiveProofOfDeliverySchedule): MutableList<String> {
        val size = item.checksum.length
        if (size > maxLength) {
            return mutableListOf<String>("checksum is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveProofOfDeliveryScheduleRequiredChecksumRule(
    var message: String,
) : AdaptiveProofOfDeliveryScheduleRule {
    override fun check(item: AdaptiveProofOfDeliverySchedule): MutableList<String> {
        if (item.checksum.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive proof of delivery schedule. */
data class AdaptiveProofOfDeliveryScheduleValidator(
    var rules: MutableList<AdaptiveProofOfDeliveryScheduleRule>,
) {
    fun validate(item: AdaptiveProofOfDeliverySchedule): MutableList<String> {
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

    fun requireValid(item: AdaptiveProofOfDeliverySchedule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveProofOfDeliverySchedule>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveProofOfDeliveryScheduleValidator(): AdaptiveProofOfDeliveryScheduleValidator {
    return AdaptiveProofOfDeliveryScheduleValidator(
        mutableListOf<AdaptiveProofOfDeliveryScheduleRule>(
            AdaptiveProofOfDeliveryScheduleRequiredSummaryRule(
                "adaptive proof of delivery schedule needs a value",
            ),
            AdaptiveProofOfDeliveryScheduleChecksumLengthRule(80),
            AdaptiveProofOfDeliveryScheduleRequiredChecksumRule(
                "adaptive proof of delivery schedule needs this field",
            ),
        ),
    )
}
