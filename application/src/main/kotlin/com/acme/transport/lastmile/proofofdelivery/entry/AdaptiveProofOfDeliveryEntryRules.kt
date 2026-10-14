package com.acme.transport.lastmile.proofofdelivery.entry

/** A rule a adaptive proof of delivery entry must pass. Check returns its problems, or none. */
interface AdaptiveProofOfDeliveryEntryRule {
    fun check(item: AdaptiveProofOfDeliveryEntry): MutableList<String>
}

data class AdaptiveProofOfDeliveryEntryRequiredTrailerIdRule(
    var message: String,
) : AdaptiveProofOfDeliveryEntryRule {
    override fun check(item: AdaptiveProofOfDeliveryEntry): MutableList<String> {
        if (item.trailerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveProofOfDeliveryEntryRetriesRangeRule(
    var min: Int,
    var max: Int,
) : AdaptiveProofOfDeliveryEntryRule {
    override fun check(item: AdaptiveProofOfDeliveryEntry): MutableList<String> {
        val value = item.retries
        if (value < min || value > max) {
            return mutableListOf<String>("retries $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveProofOfDeliveryEntryStopCountRangeRule(
    var min: Int,
    var max: Int,
) : AdaptiveProofOfDeliveryEntryRule {
    override fun check(item: AdaptiveProofOfDeliveryEntry): MutableList<String> {
        val value = item.stopCount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("stop count $value is below $min")
        }
        if (value > max) {
            problems.add("stop count $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a adaptive proof of delivery entry. */
data class AdaptiveProofOfDeliveryEntryValidator(var rules: MutableList<AdaptiveProofOfDeliveryEntryRule>) {
    fun validate(item: AdaptiveProofOfDeliveryEntry): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AdaptiveProofOfDeliveryEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveProofOfDeliveryEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveProofOfDeliveryEntryValidator(): AdaptiveProofOfDeliveryEntryValidator {
    return AdaptiveProofOfDeliveryEntryValidator(
        mutableListOf<AdaptiveProofOfDeliveryEntryRule>(
            AdaptiveProofOfDeliveryEntryRequiredTrailerIdRule("adaptive proof of delivery entry needs a value"),
            AdaptiveProofOfDeliveryEntryRetriesRangeRule(120, 255),
            AdaptiveProofOfDeliveryEntryStopCountRangeRule(3600, 3600),
        ),
    )
}
