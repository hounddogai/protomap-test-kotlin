package com.acme.transport.freightpay.ratecard.baseline

import com.acme.transport.kit.text.normalize

/** A rule a adaptive rate card baseline must pass. Check returns its problems, or none. */
interface AdaptiveRateCardBaselineRule {
    fun check(item: AdaptiveRateCardBaseline): MutableList<String>
}

data class AdaptiveRateCardBaselineVehicleIdLengthRule(var maxLength: Int) : AdaptiveRateCardBaselineRule {
    override fun check(item: AdaptiveRateCardBaseline): MutableList<String> {
        val size = item.vehicleId.length
        if (size > maxLength) {
            return mutableListOf<String>("vehicle id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveRateCardBaselineRequiredCodeRule(var message: String) : AdaptiveRateCardBaselineRule {
    override fun check(item: AdaptiveRateCardBaseline): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveRateCardBaselineAllowedChannelRule(
    var allowed: MutableList<String>,
) : AdaptiveRateCardBaselineRule {
    override fun check(item: AdaptiveRateCardBaseline): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.channel))) {
            return mutableListOf<String>("channel ${item.channel} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveRateCardBaselineNameLengthRule(var maxLength: Int) : AdaptiveRateCardBaselineRule {
    override fun check(item: AdaptiveRateCardBaseline): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive rate card baseline. */
data class AdaptiveRateCardBaselineValidator(var rules: MutableList<AdaptiveRateCardBaselineRule>) {
    fun validate(item: AdaptiveRateCardBaseline): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AdaptiveRateCardBaseline) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveRateCardBaseline>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveRateCardBaselineValidator(): AdaptiveRateCardBaselineValidator {
    return AdaptiveRateCardBaselineValidator(
        mutableListOf<AdaptiveRateCardBaselineRule>(
            AdaptiveRateCardBaselineVehicleIdLengthRule(3600),
            AdaptiveRateCardBaselineRequiredCodeRule("adaptive rate card baseline needs this field"),
            AdaptiveRateCardBaselineAllowedChannelRule(mutableListOf<String>("batched", "tiered")),
            AdaptiveRateCardBaselineNameLengthRule(64),
        ),
    )
}
