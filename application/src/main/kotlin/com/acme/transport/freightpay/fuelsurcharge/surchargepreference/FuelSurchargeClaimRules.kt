package com.acme.transport.freightpay.fuelsurcharge.surchargepreference

import com.acme.transport.kit.text.normalize

/** A rule a fuel surcharge claim must pass. Check returns its problems, or none. */
interface FuelSurchargeClaimRule {
    fun check(item: FuelSurchargeClaim): MutableList<String>
}

data class FuelSurchargeClaimRequiredDriverIdRule(var message: String) : FuelSurchargeClaimRule {
    override fun check(item: FuelSurchargeClaim): MutableList<String> {
        if (item.driverId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FuelSurchargeClaimTitleLengthRule(var maxLength: Int) : FuelSurchargeClaimRule {
    override fun check(item: FuelSurchargeClaim): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FuelSurchargeClaimAllowedDriverIdRule(var allowed: MutableList<String>) : FuelSurchargeClaimRule {
    override fun check(item: FuelSurchargeClaim): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.driverId))) {
            return mutableListOf<String>("driver id ${item.driverId} is expired")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fuel surcharge claim. */
data class FuelSurchargeClaimValidator(var rules: MutableList<FuelSurchargeClaimRule>) {
    fun validate(item: FuelSurchargeClaim): MutableList<String> {
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

    fun requireValid(item: FuelSurchargeClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FuelSurchargeClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultFuelSurchargeClaimValidator(): FuelSurchargeClaimValidator {
    return FuelSurchargeClaimValidator(
        mutableListOf<FuelSurchargeClaimRule>(
            FuelSurchargeClaimRequiredDriverIdRule("fuel surcharge claim needs a value"),
            FuelSurchargeClaimTitleLengthRule(255),
            FuelSurchargeClaimAllowedDriverIdRule(mutableListOf<String>("adaptive", "strict", "legacy")),
        ),
    )
}
