package com.acme.transport.freightpay.fuelsurcharge.window

/** A rule a global fuel surcharge baseline must pass. Check returns its problems, or none. */
interface GlobalFuelSurchargeBaselineRule {
    fun check(item: GlobalFuelSurchargeBaseline): MutableList<String>
}

data class GlobalFuelSurchargeBaselineRequiredCarrierRule(
    var message: String,
) : GlobalFuelSurchargeBaselineRule {
    override fun check(item: GlobalFuelSurchargeBaseline): MutableList<String> {
        if (item.carrier.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalFuelSurchargeBaselineCodeLengthRule(var maxLength: Int) : GlobalFuelSurchargeBaselineRule {
    override fun check(item: GlobalFuelSurchargeBaseline): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalFuelSurchargeBaselineTitleLengthRule(var maxLength: Int) : GlobalFuelSurchargeBaselineRule {
    override fun check(item: GlobalFuelSurchargeBaseline): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalFuelSurchargeBaselineCapacityRangeRule(
    var min: Int,
    var max: Int,
) : GlobalFuelSurchargeBaselineRule {
    override fun check(item: GlobalFuelSurchargeBaseline): MutableList<String> {
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

data class GlobalFuelSurchargeBaselineCostRangeRule(
    var min: Double,
    var max: Double,
) : GlobalFuelSurchargeBaselineRule {
    override fun check(item: GlobalFuelSurchargeBaseline): MutableList<String> {
        val value = item.cost
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("cost $value is below $min")
        }
        if (value > max) {
            problems.add("cost $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a global fuel surcharge baseline. */
data class GlobalFuelSurchargeBaselineValidator(var rules: MutableList<GlobalFuelSurchargeBaselineRule>) {
    fun validate(item: GlobalFuelSurchargeBaseline): MutableList<String> {
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

    fun requireValid(item: GlobalFuelSurchargeBaseline) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalFuelSurchargeBaseline>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalFuelSurchargeBaselineValidator(): GlobalFuelSurchargeBaselineValidator {
    return GlobalFuelSurchargeBaselineValidator(
        mutableListOf<GlobalFuelSurchargeBaselineRule>(
            GlobalFuelSurchargeBaselineRequiredCarrierRule("global fuel surcharge baseline needs an entry"),
            GlobalFuelSurchargeBaselineCodeLengthRule(64),
            GlobalFuelSurchargeBaselineTitleLengthRule(32),
            GlobalFuelSurchargeBaselineCapacityRangeRule(32, 64),
            GlobalFuelSurchargeBaselineCostRangeRule(8.8, 618.3),
        ),
    )
}
