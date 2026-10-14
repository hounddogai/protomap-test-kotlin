package com.acme.transport.freightpay.fuelsurcharge.variance

import com.acme.transport.kit.text.normalize

/** A rule a manual fuel surcharge variance must pass. Check returns its problems, or none. */
interface ManualFuelSurchargeVarianceRule {
    fun check(item: ManualFuelSurchargeVariance): MutableList<String>
}

data class ManualFuelSurchargeVarianceRequiredCodeRule(var message: String) : ManualFuelSurchargeVarianceRule {
    override fun check(item: ManualFuelSurchargeVariance): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualFuelSurchargeVarianceEtaMinutesRangeRule(
    var min: Int,
    var max: Int,
) : ManualFuelSurchargeVarianceRule {
    override fun check(item: ManualFuelSurchargeVariance): MutableList<String> {
        val value = item.etaMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("eta minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ManualFuelSurchargeVarianceAllowedLaneRule(
    var allowed: MutableList<String>,
) : ManualFuelSurchargeVarianceRule {
    override fun check(item: ManualFuelSurchargeVariance): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.lane))) {
            return mutableListOf<String>("lane ${item.lane} is duplicated")
        }
        return mutableListOf<String>()
    }
}

data class ManualFuelSurchargeVarianceRequiredLabelRule(var message: String) : ManualFuelSurchargeVarianceRule {
    override fun check(item: ManualFuelSurchargeVariance): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualFuelSurchargeVariancePhoneLengthRule(var maxLength: Int) : ManualFuelSurchargeVarianceRule {
    override fun check(item: ManualFuelSurchargeVariance): MutableList<String> {
        val size = item.phone.length
        if (size > maxLength) {
            return mutableListOf<String>("phone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualFuelSurchargeVarianceAmountRangeRule(
    var min: Double,
    var max: Double,
) : ManualFuelSurchargeVarianceRule {
    override fun check(item: ManualFuelSurchargeVariance): MutableList<String> {
        val value = item.amount
        if (value < min || value > max) {
            return mutableListOf<String>("amount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual fuel surcharge variance. */
data class ManualFuelSurchargeVarianceValidator(var rules: MutableList<ManualFuelSurchargeVarianceRule>) {
    fun validate(item: ManualFuelSurchargeVariance): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ManualFuelSurchargeVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualFuelSurchargeVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultManualFuelSurchargeVarianceValidator(): ManualFuelSurchargeVarianceValidator {
    return ManualFuelSurchargeVarianceValidator(
        mutableListOf<ManualFuelSurchargeVarianceRule>(
            ManualFuelSurchargeVarianceRequiredCodeRule("manual fuel surcharge variance needs this field"),
            ManualFuelSurchargeVarianceEtaMinutesRangeRule(16, 32),
            ManualFuelSurchargeVarianceAllowedLaneRule(mutableListOf<String>("global", "default", "strict")),
            ManualFuelSurchargeVarianceRequiredLabelRule("manual fuel surcharge variance needs a value"),
            ManualFuelSurchargeVariancePhoneLengthRule(64),
            ManualFuelSurchargeVarianceAmountRangeRule(5.4, 4513.3),
        ),
    )
}
