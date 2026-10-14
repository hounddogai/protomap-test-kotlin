package com.acme.transport.freightpay.fuelsurcharge.window

import com.acme.transport.kit.text.normalize

/** A rule a fuel surcharge milestone must pass. Check returns its problems, or none. */
interface FuelSurchargeMilestoneRule {
    fun check(item: FuelSurchargeMilestone): MutableList<String>
}

data class FuelSurchargeMilestoneRequiredChecksumRule(var message: String) : FuelSurchargeMilestoneRule {
    override fun check(item: FuelSurchargeMilestone): MutableList<String> {
        if (item.checksum.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FuelSurchargeMilestoneAllowedLabelRule(
    var allowed: MutableList<String>,
) : FuelSurchargeMilestoneRule {
    override fun check(item: FuelSurchargeMilestone): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class FuelSurchargeMilestoneCurrencyLengthRule(var maxLength: Int) : FuelSurchargeMilestoneRule {
    override fun check(item: FuelSurchargeMilestone): MutableList<String> {
        val size = item.currency.length
        if (size > maxLength) {
            return mutableListOf<String>("currency is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FuelSurchargeMilestoneCodeLengthRule(var maxLength: Int) : FuelSurchargeMilestoneRule {
    override fun check(item: FuelSurchargeMilestone): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FuelSurchargeMilestoneCodePrefixRule(var prefix: String) : FuelSurchargeMilestoneRule {
    override fun check(item: FuelSurchargeMilestone): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class FuelSurchargeMilestoneVersionRangeRule(var min: Int, var max: Int) : FuelSurchargeMilestoneRule {
    override fun check(item: FuelSurchargeMilestone): MutableList<String> {
        val value = item.version
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("version $value is below $min")
        }
        if (value > max) {
            problems.add("version $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a fuel surcharge milestone. */
data class FuelSurchargeMilestoneValidator(var rules: MutableList<FuelSurchargeMilestoneRule>) {
    fun validate(item: FuelSurchargeMilestone): MutableList<String> {
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

    fun requireValid(item: FuelSurchargeMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FuelSurchargeMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultFuelSurchargeMilestoneValidator(): FuelSurchargeMilestoneValidator {
    return FuelSurchargeMilestoneValidator(
        mutableListOf<FuelSurchargeMilestoneRule>(
            FuelSurchargeMilestoneRequiredChecksumRule("fuel surcharge milestone needs this field"),
            FuelSurchargeMilestoneAllowedLabelRule(mutableListOf<String>("capped")),
            FuelSurchargeMilestoneCurrencyLengthRule(1000),
            FuelSurchargeMilestoneCodeLengthRule(1000),
            FuelSurchargeMilestoneCodePrefixRule("Q"),
            FuelSurchargeMilestoneVersionRangeRule(3600, 86400),
        ),
    )
}
