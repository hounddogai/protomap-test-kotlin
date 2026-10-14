package com.acme.transport.freightpay.fuelsurcharge.adjustment

/** A rule a fuel surcharge bundle must pass. Check returns its problems, or none. */
interface FuelSurchargeBundleRule {
    fun check(item: FuelSurchargeBundle): MutableList<String>
}

data class FuelSurchargeBundleRequiredCodeRule(var message: String) : FuelSurchargeBundleRule {
    override fun check(item: FuelSurchargeBundle): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FuelSurchargeBundleRequiredNameRule(var message: String) : FuelSurchargeBundleRule {
    override fun check(item: FuelSurchargeBundle): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FuelSurchargeBundleDistanceKmRangeRule(var min: Double, var max: Double) : FuelSurchargeBundleRule {
    override fun check(item: FuelSurchargeBundle): MutableList<String> {
        val value = item.distanceKm
        if (value < min || value > max) {
            return mutableListOf<String>("distance km $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fuel surcharge bundle. */
data class FuelSurchargeBundleValidator(var rules: MutableList<FuelSurchargeBundleRule>) {
    fun validate(item: FuelSurchargeBundle): MutableList<String> {
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

    fun requireValid(item: FuelSurchargeBundle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FuelSurchargeBundle>): MutableMap<String, MutableList<String>> {
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

fun defaultFuelSurchargeBundleValidator(): FuelSurchargeBundleValidator {
    return FuelSurchargeBundleValidator(
        mutableListOf<FuelSurchargeBundleRule>(
            FuelSurchargeBundleRequiredCodeRule("fuel surcharge bundle needs an entry"),
            FuelSurchargeBundleRequiredNameRule("fuel surcharge bundle needs this field"),
            FuelSurchargeBundleDistanceKmRangeRule(4.6, 1480.9),
        ),
    )
}
