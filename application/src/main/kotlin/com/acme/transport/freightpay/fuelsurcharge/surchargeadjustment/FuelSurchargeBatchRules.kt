package com.acme.transport.freightpay.fuelsurcharge.surchargeadjustment

import com.acme.transport.kit.text.normalize

/** A rule a fuel surcharge batch must pass. Check returns its problems, or none. */
interface FuelSurchargeBatchRule {
    fun check(item: FuelSurchargeBatch): MutableList<String>
}

data class FuelSurchargeBatchRouteCodeLengthRule(var maxLength: Int) : FuelSurchargeBatchRule {
    override fun check(item: FuelSurchargeBatch): MutableList<String> {
        val size = item.routeCode.length
        if (size > maxLength) {
            return mutableListOf<String>("route code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FuelSurchargeBatchRequiredDriverIdRule(var message: String) : FuelSurchargeBatchRule {
    override fun check(item: FuelSurchargeBatch): MutableList<String> {
        if (item.driverId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FuelSurchargeBatchRatioRangeRule(var min: Double, var max: Double) : FuelSurchargeBatchRule {
    override fun check(item: FuelSurchargeBatch): MutableList<String> {
        val value = item.ratio
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ratio $value is below $min")
        }
        if (value > max) {
            problems.add("ratio $value is above $max")
        }
        return problems
    }
}

data class FuelSurchargeBatchAllowedNameRule(var allowed: MutableList<String>) : FuelSurchargeBatchRule {
    override fun check(item: FuelSurchargeBatch): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is missing")
        }
        return mutableListOf<String>()
    }
}

data class FuelSurchargeBatchNameLengthRule(var maxLength: Int) : FuelSurchargeBatchRule {
    override fun check(item: FuelSurchargeBatch): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FuelSurchargeBatchCodeLengthRule(var maxLength: Int) : FuelSurchargeBatchRule {
    override fun check(item: FuelSurchargeBatch): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fuel surcharge batch. */
data class FuelSurchargeBatchValidator(var rules: MutableList<FuelSurchargeBatchRule>) {
    fun validate(item: FuelSurchargeBatch): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FuelSurchargeBatch) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FuelSurchargeBatch>): MutableMap<String, MutableList<String>> {
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

fun defaultFuelSurchargeBatchValidator(): FuelSurchargeBatchValidator {
    return FuelSurchargeBatchValidator(
        mutableListOf<FuelSurchargeBatchRule>(
            FuelSurchargeBatchRouteCodeLengthRule(1000),
            FuelSurchargeBatchRequiredDriverIdRule("fuel surcharge batch needs a value"),
            FuelSurchargeBatchRatioRangeRule(1.7, 4824.5),
            FuelSurchargeBatchAllowedNameRule(mutableListOf<String>("global", "pending", "pending")),
            FuelSurchargeBatchNameLengthRule(86400),
            FuelSurchargeBatchCodeLengthRule(86400),
        ),
    )
}
