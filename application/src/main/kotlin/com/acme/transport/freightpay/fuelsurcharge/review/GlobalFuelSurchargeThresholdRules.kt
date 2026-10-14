package com.acme.transport.freightpay.fuelsurcharge.review

/** A rule a global fuel surcharge threshold must pass. Check returns its problems, or none. */
interface GlobalFuelSurchargeThresholdRule {
    fun check(item: GlobalFuelSurchargeThreshold): MutableList<String>
}

data class GlobalFuelSurchargeThresholdShiftCodeLengthRule(
    var maxLength: Int,
) : GlobalFuelSurchargeThresholdRule {
    override fun check(item: GlobalFuelSurchargeThreshold): MutableList<String> {
        val size = item.shiftCode.length
        if (size > maxLength) {
            return mutableListOf<String>("shift code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalFuelSurchargeThresholdRequiredContactEmailRule(
    var message: String,
) : GlobalFuelSurchargeThresholdRule {
    override fun check(item: GlobalFuelSurchargeThreshold): MutableList<String> {
        if (item.contactEmail.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalFuelSurchargeThresholdCodeLengthRule(var maxLength: Int) : GlobalFuelSurchargeThresholdRule {
    override fun check(item: GlobalFuelSurchargeThreshold): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalFuelSurchargeThresholdRequiredShiftCodeRule(
    var message: String,
) : GlobalFuelSurchargeThresholdRule {
    override fun check(item: GlobalFuelSurchargeThreshold): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalFuelSurchargeThresholdQuantityRangeRule(
    var min: Int,
    var max: Int,
) : GlobalFuelSurchargeThresholdRule {
    override fun check(item: GlobalFuelSurchargeThreshold): MutableList<String> {
        val value = item.quantity
        if (value < min || value > max) {
            return mutableListOf<String>("quantity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global fuel surcharge threshold. */
data class GlobalFuelSurchargeThresholdValidator(var rules: MutableList<GlobalFuelSurchargeThresholdRule>) {
    fun validate(item: GlobalFuelSurchargeThreshold): MutableList<String> {
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

    fun requireValid(item: GlobalFuelSurchargeThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalFuelSurchargeThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalFuelSurchargeThresholdValidator(): GlobalFuelSurchargeThresholdValidator {
    return GlobalFuelSurchargeThresholdValidator(
        mutableListOf<GlobalFuelSurchargeThresholdRule>(
            GlobalFuelSurchargeThresholdShiftCodeLengthRule(120),
            GlobalFuelSurchargeThresholdRequiredContactEmailRule(
                "global fuel surcharge threshold needs this field",
            ),
            GlobalFuelSurchargeThresholdCodeLengthRule(3600),
            GlobalFuelSurchargeThresholdRequiredShiftCodeRule("global fuel surcharge threshold needs an entry"),
            GlobalFuelSurchargeThresholdQuantityRangeRule(120, 255),
        ),
    )
}
