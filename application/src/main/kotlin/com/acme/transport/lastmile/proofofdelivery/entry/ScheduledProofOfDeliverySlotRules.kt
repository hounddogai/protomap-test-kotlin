package com.acme.transport.lastmile.proofofdelivery.entry

/** A rule a scheduled proof of delivery slot must pass. Check returns its problems, or none. */
interface ScheduledProofOfDeliverySlotRule {
    fun check(item: ScheduledProofOfDeliverySlot): MutableList<String>
}

data class ScheduledProofOfDeliverySlotCodePrefixRule(var prefix: String) : ScheduledProofOfDeliverySlotRule {
    override fun check(item: ScheduledProofOfDeliverySlot): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledProofOfDeliverySlotRequiredChecksumRule(
    var message: String,
) : ScheduledProofOfDeliverySlotRule {
    override fun check(item: ScheduledProofOfDeliverySlot): MutableList<String> {
        if (item.checksum.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledProofOfDeliverySlotCountryLengthRule(
    var maxLength: Int,
) : ScheduledProofOfDeliverySlotRule {
    override fun check(item: ScheduledProofOfDeliverySlot): MutableList<String> {
        val size = item.country.length
        if (size > maxLength) {
            return mutableListOf<String>("country is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledProofOfDeliverySlotVisibleGuardRule(var limit: Int) : ScheduledProofOfDeliverySlotRule {
    override fun check(item: ScheduledProofOfDeliverySlot): MutableList<String> {
        if (item.visible && item.stopCount > limit) {
            return mutableListOf<String>("stop count ${item.stopCount} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledProofOfDeliverySlotEtaMinutesRangeRule(
    var min: Int,
    var max: Int,
) : ScheduledProofOfDeliverySlotRule {
    override fun check(item: ScheduledProofOfDeliverySlot): MutableList<String> {
        val value = item.etaMinutes
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("eta minutes $value is below $min")
        }
        if (value > max) {
            problems.add("eta minutes $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a scheduled proof of delivery slot. */
data class ScheduledProofOfDeliverySlotValidator(var rules: MutableList<ScheduledProofOfDeliverySlotRule>) {
    fun validate(item: ScheduledProofOfDeliverySlot): MutableList<String> {
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

    fun requireValid(item: ScheduledProofOfDeliverySlot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledProofOfDeliverySlot>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledProofOfDeliverySlotValidator(): ScheduledProofOfDeliverySlotValidator {
    return ScheduledProofOfDeliverySlotValidator(
        mutableListOf<ScheduledProofOfDeliverySlotRule>(
            ScheduledProofOfDeliverySlotCodePrefixRule("OPS"),
            ScheduledProofOfDeliverySlotRequiredChecksumRule("scheduled proof of delivery slot needs an entry"),
            ScheduledProofOfDeliverySlotCountryLengthRule(86400),
            ScheduledProofOfDeliverySlotVisibleGuardRule(32),
            ScheduledProofOfDeliverySlotEtaMinutesRangeRule(64, 64),
        ),
    )
}
