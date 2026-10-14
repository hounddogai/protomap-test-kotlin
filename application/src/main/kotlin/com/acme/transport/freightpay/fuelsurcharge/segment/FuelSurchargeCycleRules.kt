package com.acme.transport.freightpay.fuelsurcharge.segment

import com.acme.transport.kit.text.normalize

/** A rule a fuel surcharge cycle must pass. Check returns its problems, or none. */
interface FuelSurchargeCycleRule {
    fun check(item: FuelSurchargeCycle): MutableList<String>
}

data class FuelSurchargeCycleUnitLengthRule(var maxLength: Int) : FuelSurchargeCycleRule {
    override fun check(item: FuelSurchargeCycle): MutableList<String> {
        val size = item.unit.length
        if (size > maxLength) {
            return mutableListOf<String>("unit is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FuelSurchargeCycleAllowedCodeRule(var allowed: MutableList<String>) : FuelSurchargeCycleRule {
    override fun check(item: FuelSurchargeCycle): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class FuelSurchargeCycleRequiredCodeRule(var message: String) : FuelSurchargeCycleRule {
    override fun check(item: FuelSurchargeCycle): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fuel surcharge cycle. */
data class FuelSurchargeCycleValidator(var rules: MutableList<FuelSurchargeCycleRule>) {
    fun validate(item: FuelSurchargeCycle): MutableList<String> {
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

    fun requireValid(item: FuelSurchargeCycle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FuelSurchargeCycle>): MutableMap<String, MutableList<String>> {
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

fun defaultFuelSurchargeCycleValidator(): FuelSurchargeCycleValidator {
    return FuelSurchargeCycleValidator(
        mutableListOf<FuelSurchargeCycleRule>(
            FuelSurchargeCycleUnitLengthRule(3600),
            FuelSurchargeCycleAllowedCodeRule(mutableListOf<String>("default", "weighted", "adaptive")),
            FuelSurchargeCycleRequiredCodeRule("fuel surcharge cycle needs this field"),
        ),
    )
}
