package com.acme.transport.freightpay.fuelsurcharge.entry

/** A rule a fuel surcharge entry must pass. Check returns its problems, or none. */
interface FuelSurchargeEntryRule {
    fun check(item: FuelSurchargeEntry): MutableList<String>
}

data class FuelSurchargeEntryThresholdRangeRule(var min: Double, var max: Double) : FuelSurchargeEntryRule {
    override fun check(item: FuelSurchargeEntry): MutableList<String> {
        val value = item.threshold
        if (value < min || value > max) {
            return mutableListOf<String>("threshold $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FuelSurchargeEntryTitleLengthRule(var maxLength: Int) : FuelSurchargeEntryRule {
    override fun check(item: FuelSurchargeEntry): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FuelSurchargeEntryRequiredCodeRule(var message: String) : FuelSurchargeEntryRule {
    override fun check(item: FuelSurchargeEntry): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FuelSurchargeEntryCodeLengthRule(var maxLength: Int) : FuelSurchargeEntryRule {
    override fun check(item: FuelSurchargeEntry): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fuel surcharge entry. */
data class FuelSurchargeEntryValidator(var rules: MutableList<FuelSurchargeEntryRule>) {
    fun validate(item: FuelSurchargeEntry): MutableList<String> {
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

    fun requireValid(item: FuelSurchargeEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FuelSurchargeEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultFuelSurchargeEntryValidator(): FuelSurchargeEntryValidator {
    return FuelSurchargeEntryValidator(
        mutableListOf<FuelSurchargeEntryRule>(
            FuelSurchargeEntryThresholdRangeRule(5.4, 2044.3),
            FuelSurchargeEntryTitleLengthRule(255),
            FuelSurchargeEntryRequiredCodeRule("fuel surcharge entry needs an entry"),
            FuelSurchargeEntryCodeLengthRule(86400),
        ),
    )
}
