package com.acme.transport.freightpay.fuelsurcharge.surchargebaseline

/** A rule a fuel surcharge override must pass. Check returns its problems, or none. */
interface FuelSurchargeOverrideRule {
    fun check(item: FuelSurchargeOverride): MutableList<String>
}

data class FuelSurchargeOverrideCodeLengthRule(var maxLength: Int) : FuelSurchargeOverrideRule {
    override fun check(item: FuelSurchargeOverride): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FuelSurchargeOverrideSegmentLengthRule(var maxLength: Int) : FuelSurchargeOverrideRule {
    override fun check(item: FuelSurchargeOverride): MutableList<String> {
        val size = item.segment.length
        if (size > maxLength) {
            return mutableListOf<String>("segment is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FuelSurchargeOverrideRequiredUnitRule(var message: String) : FuelSurchargeOverrideRule {
    override fun check(item: FuelSurchargeOverride): MutableList<String> {
        if (item.unit.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fuel surcharge override. */
data class FuelSurchargeOverrideValidator(var rules: MutableList<FuelSurchargeOverrideRule>) {
    fun validate(item: FuelSurchargeOverride): MutableList<String> {
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

    fun requireValid(item: FuelSurchargeOverride) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FuelSurchargeOverride>): MutableMap<String, MutableList<String>> {
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

fun defaultFuelSurchargeOverrideValidator(): FuelSurchargeOverrideValidator {
    return FuelSurchargeOverrideValidator(
        mutableListOf<FuelSurchargeOverrideRule>(
            FuelSurchargeOverrideCodeLengthRule(1000),
            FuelSurchargeOverrideSegmentLengthRule(16),
            FuelSurchargeOverrideRequiredUnitRule("fuel surcharge override needs an entry"),
        ),
    )
}
