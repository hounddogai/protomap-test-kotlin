package com.acme.transport.lastmile.proofofdelivery.estimate

/** A rule a legacy proof of delivery window must pass. Check returns its problems, or none. */
interface LegacyProofOfDeliveryWindowRule {
    fun check(item: LegacyProofOfDeliveryWindow): MutableList<String>
}

data class LegacyProofOfDeliveryWindowRequiredTrailerIdRule(
    var message: String,
) : LegacyProofOfDeliveryWindowRule {
    override fun check(item: LegacyProofOfDeliveryWindow): MutableList<String> {
        if (item.trailerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyProofOfDeliveryWindowRetriesRangeRule(
    var min: Int,
    var max: Int,
) : LegacyProofOfDeliveryWindowRule {
    override fun check(item: LegacyProofOfDeliveryWindow): MutableList<String> {
        val value = item.retries
        if (value < min || value > max) {
            return mutableListOf<String>("retries $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LegacyProofOfDeliveryWindowRequiredTitleRule(var message: String) : LegacyProofOfDeliveryWindowRule {
    override fun check(item: LegacyProofOfDeliveryWindow): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyProofOfDeliveryWindowStopCountRangeRule(
    var min: Int,
    var max: Int,
) : LegacyProofOfDeliveryWindowRule {
    override fun check(item: LegacyProofOfDeliveryWindow): MutableList<String> {
        val value = item.stopCount
        if (value < min || value > max) {
            return mutableListOf<String>("stop count $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LegacyProofOfDeliveryWindowCapacityRangeRule(
    var min: Int,
    var max: Int,
) : LegacyProofOfDeliveryWindowRule {
    override fun check(item: LegacyProofOfDeliveryWindow): MutableList<String> {
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

data class LegacyProofOfDeliveryWindowDiscountRangeRule(
    var min: Double,
    var max: Double,
) : LegacyProofOfDeliveryWindowRule {
    override fun check(item: LegacyProofOfDeliveryWindow): MutableList<String> {
        val value = item.discount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("discount $value is below $min")
        }
        if (value > max) {
            problems.add("discount $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a legacy proof of delivery window. */
data class LegacyProofOfDeliveryWindowValidator(var rules: MutableList<LegacyProofOfDeliveryWindowRule>) {
    fun validate(item: LegacyProofOfDeliveryWindow): MutableList<String> {
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

    fun requireValid(item: LegacyProofOfDeliveryWindow) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyProofOfDeliveryWindow>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyProofOfDeliveryWindowValidator(): LegacyProofOfDeliveryWindowValidator {
    return LegacyProofOfDeliveryWindowValidator(
        mutableListOf<LegacyProofOfDeliveryWindowRule>(
            LegacyProofOfDeliveryWindowRequiredTrailerIdRule("legacy proof of delivery window needs this field"),
            LegacyProofOfDeliveryWindowRetriesRangeRule(64, 255),
            LegacyProofOfDeliveryWindowRequiredTitleRule("legacy proof of delivery window needs an entry"),
            LegacyProofOfDeliveryWindowStopCountRangeRule(32, 32),
            LegacyProofOfDeliveryWindowCapacityRangeRule(16, 3600),
            LegacyProofOfDeliveryWindowDiscountRangeRule(4.6, 826.5),
        ),
    )
}
