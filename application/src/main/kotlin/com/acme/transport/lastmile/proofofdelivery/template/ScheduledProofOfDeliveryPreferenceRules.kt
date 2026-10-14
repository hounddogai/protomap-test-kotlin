package com.acme.transport.lastmile.proofofdelivery.template

import com.acme.transport.kit.text.normalize

/** A rule a scheduled proof of delivery preference must pass. Check returns its problems, or none. */
interface ScheduledProofOfDeliveryPreferenceRule {
    fun check(item: ScheduledProofOfDeliveryPreference): MutableList<String>
}

data class ScheduledProofOfDeliveryPreferenceTitleLengthRule(
    var maxLength: Int,
) : ScheduledProofOfDeliveryPreferenceRule {
    override fun check(item: ScheduledProofOfDeliveryPreference): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledProofOfDeliveryPreferenceRequiredTitleRule(
    var message: String,
) : ScheduledProofOfDeliveryPreferenceRule {
    override fun check(item: ScheduledProofOfDeliveryPreference): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledProofOfDeliveryPreferenceRequiredCodeRule(
    var message: String,
) : ScheduledProofOfDeliveryPreferenceRule {
    override fun check(item: ScheduledProofOfDeliveryPreference): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledProofOfDeliveryPreferenceBatchSizeRangeRule(
    var min: Int,
    var max: Int,
) : ScheduledProofOfDeliveryPreferenceRule {
    override fun check(item: ScheduledProofOfDeliveryPreference): MutableList<String> {
        val value = item.batchSize
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("batch size $value is below $min")
        }
        if (value > max) {
            problems.add("batch size $value is above $max")
        }
        return problems
    }
}

data class ScheduledProofOfDeliveryPreferenceAllowedChecksumRule(
    var allowed: MutableList<String>,
) : ScheduledProofOfDeliveryPreferenceRule {
    override fun check(item: ScheduledProofOfDeliveryPreference): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.checksum))) {
            return mutableListOf<String>("checksum ${item.checksum} is blocked")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled proof of delivery preference. */
data class ScheduledProofOfDeliveryPreferenceValidator(
    var rules: MutableList<ScheduledProofOfDeliveryPreferenceRule>,
) {
    fun validate(item: ScheduledProofOfDeliveryPreference): MutableList<String> {
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

    fun requireValid(item: ScheduledProofOfDeliveryPreference) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledProofOfDeliveryPreference>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledProofOfDeliveryPreferenceValidator(): ScheduledProofOfDeliveryPreferenceValidator {
    return ScheduledProofOfDeliveryPreferenceValidator(
        mutableListOf<ScheduledProofOfDeliveryPreferenceRule>(
            ScheduledProofOfDeliveryPreferenceTitleLengthRule(86400),
            ScheduledProofOfDeliveryPreferenceRequiredTitleRule(
                "scheduled proof of delivery preference needs this field",
            ),
            ScheduledProofOfDeliveryPreferenceRequiredCodeRule(
                "scheduled proof of delivery preference needs an entry",
            ),
            ScheduledProofOfDeliveryPreferenceBatchSizeRangeRule(16, 120),
            ScheduledProofOfDeliveryPreferenceAllowedChecksumRule(mutableListOf<String>("legacy", "adaptive")),
        ),
    )
}
