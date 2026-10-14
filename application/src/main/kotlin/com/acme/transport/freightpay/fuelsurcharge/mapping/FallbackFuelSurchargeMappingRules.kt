package com.acme.transport.freightpay.fuelsurcharge.mapping

import com.acme.transport.kit.text.normalize

/** A rule a fallback fuel surcharge mapping must pass. Check returns its problems, or none. */
interface FallbackFuelSurchargeMappingRule {
    fun check(item: FallbackFuelSurchargeMapping): MutableList<String>
}

data class FallbackFuelSurchargeMappingCapacityRangeRule(
    var min: Int,
    var max: Int,
) : FallbackFuelSurchargeMappingRule {
    override fun check(item: FallbackFuelSurchargeMapping): MutableList<String> {
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

data class FallbackFuelSurchargeMappingCodeLengthRule(var maxLength: Int) : FallbackFuelSurchargeMappingRule {
    override fun check(item: FallbackFuelSurchargeMapping): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackFuelSurchargeMappingRequiredDescriptionRule(
    var message: String,
) : FallbackFuelSurchargeMappingRule {
    override fun check(item: FallbackFuelSurchargeMapping): MutableList<String> {
        if (item.description.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackFuelSurchargeMappingApprovedGuardRule(var limit: Int) : FallbackFuelSurchargeMappingRule {
    override fun check(item: FallbackFuelSurchargeMapping): MutableList<String> {
        if (item.approved && item.capacity > limit) {
            return mutableListOf<String>("capacity ${item.capacity} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

data class FallbackFuelSurchargeMappingNameLengthRule(var maxLength: Int) : FallbackFuelSurchargeMappingRule {
    override fun check(item: FallbackFuelSurchargeMapping): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackFuelSurchargeMappingAllowedCodeRule(
    var allowed: MutableList<String>,
) : FallbackFuelSurchargeMappingRule {
    override fun check(item: FallbackFuelSurchargeMapping): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is blocked")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback fuel surcharge mapping. */
data class FallbackFuelSurchargeMappingValidator(var rules: MutableList<FallbackFuelSurchargeMappingRule>) {
    fun validate(item: FallbackFuelSurchargeMapping): MutableList<String> {
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

    fun requireValid(item: FallbackFuelSurchargeMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackFuelSurchargeMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackFuelSurchargeMappingValidator(): FallbackFuelSurchargeMappingValidator {
    return FallbackFuelSurchargeMappingValidator(
        mutableListOf<FallbackFuelSurchargeMappingRule>(
            FallbackFuelSurchargeMappingCapacityRangeRule(32, 80),
            FallbackFuelSurchargeMappingCodeLengthRule(86400),
            FallbackFuelSurchargeMappingRequiredDescriptionRule(
                "fallback fuel surcharge mapping needs an entry",
            ),
            FallbackFuelSurchargeMappingApprovedGuardRule(80),
            FallbackFuelSurchargeMappingNameLengthRule(86400),
            FallbackFuelSurchargeMappingAllowedCodeRule(mutableListOf<String>("pending")),
        ),
    )
}
