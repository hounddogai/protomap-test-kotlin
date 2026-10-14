package com.acme.transport.freightpay.fuelsurcharge.rule

import com.acme.transport.kit.text.normalize

/** A rule a pending fuel surcharge variance must pass. Check returns its problems, or none. */
interface PendingFuelSurchargeVarianceRule {
    fun check(item: PendingFuelSurchargeVariance): MutableList<String>
}

data class PendingFuelSurchargeVarianceRequiredExternalRefRule(
    var message: String,
) : PendingFuelSurchargeVarianceRule {
    override fun check(item: PendingFuelSurchargeVariance): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingFuelSurchargeVarianceBudgetRangeRule(
    var min: Double,
    var max: Double,
) : PendingFuelSurchargeVarianceRule {
    override fun check(item: PendingFuelSurchargeVariance): MutableList<String> {
        val value = item.budget
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("budget $value is below $min")
        }
        if (value > max) {
            problems.add("budget $value is above $max")
        }
        return problems
    }
}

data class PendingFuelSurchargeVarianceAllowedPhoneRule(
    var allowed: MutableList<String>,
) : PendingFuelSurchargeVarianceRule {
    override fun check(item: PendingFuelSurchargeVariance): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.phone))) {
            return mutableListOf<String>("phone ${item.phone} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class PendingFuelSurchargeVarianceRequiredTitleRule(
    var message: String,
) : PendingFuelSurchargeVarianceRule {
    override fun check(item: PendingFuelSurchargeVariance): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingFuelSurchargeVariancePhoneLengthRule(var maxLength: Int) : PendingFuelSurchargeVarianceRule {
    override fun check(item: PendingFuelSurchargeVariance): MutableList<String> {
        val size = item.phone.length
        if (size > maxLength) {
            return mutableListOf<String>("phone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending fuel surcharge variance. */
data class PendingFuelSurchargeVarianceValidator(var rules: MutableList<PendingFuelSurchargeVarianceRule>) {
    fun validate(item: PendingFuelSurchargeVariance): MutableList<String> {
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

    fun requireValid(item: PendingFuelSurchargeVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingFuelSurchargeVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingFuelSurchargeVarianceValidator(): PendingFuelSurchargeVarianceValidator {
    return PendingFuelSurchargeVarianceValidator(
        mutableListOf<PendingFuelSurchargeVarianceRule>(
            PendingFuelSurchargeVarianceRequiredExternalRefRule("pending fuel surcharge variance needs a value"),
            PendingFuelSurchargeVarianceBudgetRangeRule(0.6, 1007.8),
            PendingFuelSurchargeVarianceAllowedPhoneRule(mutableListOf<String>()),
            PendingFuelSurchargeVarianceRequiredTitleRule("pending fuel surcharge variance needs a value"),
            PendingFuelSurchargeVariancePhoneLengthRule(86400),
        ),
    )
}
