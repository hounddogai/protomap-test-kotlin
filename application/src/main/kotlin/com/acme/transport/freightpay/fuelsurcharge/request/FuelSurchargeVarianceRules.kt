package com.acme.transport.freightpay.fuelsurcharge.request

/** A rule a fuel surcharge variance must pass. Check returns its problems, or none. */
interface FuelSurchargeVarianceRule {
    fun check(item: FuelSurchargeVariance): MutableList<String>
}

data class FuelSurchargeVarianceExpiresAtRangeRule(var min: Int, var max: Int) : FuelSurchargeVarianceRule {
    override fun check(item: FuelSurchargeVariance): MutableList<String> {
        val value = item.expiresAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("expires at $value is below $min")
        }
        if (value > max) {
            problems.add("expires at $value is above $max")
        }
        return problems
    }
}

data class FuelSurchargeVarianceRequiredLabelRule(var message: String) : FuelSurchargeVarianceRule {
    override fun check(item: FuelSurchargeVariance): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FuelSurchargeVarianceRequiredChecksumRule(var message: String) : FuelSurchargeVarianceRule {
    override fun check(item: FuelSurchargeVariance): MutableList<String> {
        if (item.checksum.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fuel surcharge variance. */
data class FuelSurchargeVarianceValidator(var rules: MutableList<FuelSurchargeVarianceRule>) {
    fun validate(item: FuelSurchargeVariance): MutableList<String> {
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

    fun requireValid(item: FuelSurchargeVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FuelSurchargeVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultFuelSurchargeVarianceValidator(): FuelSurchargeVarianceValidator {
    return FuelSurchargeVarianceValidator(
        mutableListOf<FuelSurchargeVarianceRule>(
            FuelSurchargeVarianceExpiresAtRangeRule(80, 255),
            FuelSurchargeVarianceRequiredLabelRule("fuel surcharge variance needs an entry"),
            FuelSurchargeVarianceRequiredChecksumRule("fuel surcharge variance needs an entry"),
        ),
    )
}
