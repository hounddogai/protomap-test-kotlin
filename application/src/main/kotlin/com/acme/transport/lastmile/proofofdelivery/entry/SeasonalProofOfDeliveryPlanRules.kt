package com.acme.transport.lastmile.proofofdelivery.entry

import com.acme.transport.kit.text.normalize

/** A rule a seasonal proof of delivery plan must pass. Check returns its problems, or none. */
interface SeasonalProofOfDeliveryPlanRule {
    fun check(item: SeasonalProofOfDeliveryPlan): MutableList<String>
}

data class SeasonalProofOfDeliveryPlanRequiredLocaleRule(
    var message: String,
) : SeasonalProofOfDeliveryPlanRule {
    override fun check(item: SeasonalProofOfDeliveryPlan): MutableList<String> {
        if (item.locale.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalProofOfDeliveryPlanNameLengthRule(var maxLength: Int) : SeasonalProofOfDeliveryPlanRule {
    override fun check(item: SeasonalProofOfDeliveryPlan): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalProofOfDeliveryPlanAllowedChecksumRule(
    var allowed: MutableList<String>,
) : SeasonalProofOfDeliveryPlanRule {
    override fun check(item: SeasonalProofOfDeliveryPlan): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.checksum))) {
            return mutableListOf<String>("checksum ${item.checksum} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalProofOfDeliveryPlanCodePrefixRule(var prefix: String) : SeasonalProofOfDeliveryPlanRule {
    override fun check(item: SeasonalProofOfDeliveryPlan): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalProofOfDeliveryPlanLocaleLengthRule(var maxLength: Int) : SeasonalProofOfDeliveryPlanRule {
    override fun check(item: SeasonalProofOfDeliveryPlan): MutableList<String> {
        val size = item.locale.length
        if (size > maxLength) {
            return mutableListOf<String>("locale is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal proof of delivery plan. */
data class SeasonalProofOfDeliveryPlanValidator(var rules: MutableList<SeasonalProofOfDeliveryPlanRule>) {
    fun validate(item: SeasonalProofOfDeliveryPlan): MutableList<String> {
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

    fun requireValid(item: SeasonalProofOfDeliveryPlan) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalProofOfDeliveryPlan>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalProofOfDeliveryPlanValidator(): SeasonalProofOfDeliveryPlanValidator {
    return SeasonalProofOfDeliveryPlanValidator(
        mutableListOf<SeasonalProofOfDeliveryPlanRule>(
            SeasonalProofOfDeliveryPlanRequiredLocaleRule("seasonal proof of delivery plan needs an entry"),
            SeasonalProofOfDeliveryPlanNameLengthRule(120),
            SeasonalProofOfDeliveryPlanAllowedChecksumRule(
                mutableListOf<String>("lenient", "adaptive", "fallback"),
            ),
            SeasonalProofOfDeliveryPlanCodePrefixRule("Q"),
            SeasonalProofOfDeliveryPlanLocaleLengthRule(3600),
        ),
    )
}
