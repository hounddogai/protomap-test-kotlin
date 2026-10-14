package com.acme.transport.freightpay.fuelsurcharge.variance

import com.acme.transport.kit.text.normalize

/** A rule a lenient fuel surcharge bundle must pass. Check returns its problems, or none. */
interface LenientFuelSurchargeBundleRule {
    fun check(item: LenientFuelSurchargeBundle): MutableList<String>
}

data class LenientFuelSurchargeBundleTitleLengthRule(var maxLength: Int) : LenientFuelSurchargeBundleRule {
    override fun check(item: LenientFuelSurchargeBundle): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientFuelSurchargeBundleAllowedCodeRule(
    var allowed: MutableList<String>,
) : LenientFuelSurchargeBundleRule {
    override fun check(item: LenientFuelSurchargeBundle): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is duplicated")
        }
        return mutableListOf<String>()
    }
}

data class LenientFuelSurchargeBundleRateRangeRule(
    var min: Double,
    var max: Double,
) : LenientFuelSurchargeBundleRule {
    override fun check(item: LenientFuelSurchargeBundle): MutableList<String> {
        val value = item.rate
        if (value < min || value > max) {
            return mutableListOf<String>("rate $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LenientFuelSurchargeBundleCodePrefixRule(var prefix: String) : LenientFuelSurchargeBundleRule {
    override fun check(item: LenientFuelSurchargeBundle): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LenientFuelSurchargeBundleRequiredCodeRule(var message: String) : LenientFuelSurchargeBundleRule {
    override fun check(item: LenientFuelSurchargeBundle): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientFuelSurchargeBundleArchivedGuardRule(var limit: Double) : LenientFuelSurchargeBundleRule {
    override fun check(item: LenientFuelSurchargeBundle): MutableList<String> {
        if (item.archived && item.rate > limit) {
            return mutableListOf<String>("rate ${item.rate} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient fuel surcharge bundle. */
data class LenientFuelSurchargeBundleValidator(var rules: MutableList<LenientFuelSurchargeBundleRule>) {
    fun validate(item: LenientFuelSurchargeBundle): MutableList<String> {
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

    fun requireValid(item: LenientFuelSurchargeBundle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientFuelSurchargeBundle>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientFuelSurchargeBundleValidator(): LenientFuelSurchargeBundleValidator {
    return LenientFuelSurchargeBundleValidator(
        mutableListOf<LenientFuelSurchargeBundleRule>(
            LenientFuelSurchargeBundleTitleLengthRule(255),
            LenientFuelSurchargeBundleAllowedCodeRule(mutableListOf<String>()),
            LenientFuelSurchargeBundleRateRangeRule(0.1, 352.1),
            LenientFuelSurchargeBundleCodePrefixRule("OPS"),
            LenientFuelSurchargeBundleRequiredCodeRule("lenient fuel surcharge bundle needs this field"),
            LenientFuelSurchargeBundleArchivedGuardRule(8.7),
        ),
    )
}
