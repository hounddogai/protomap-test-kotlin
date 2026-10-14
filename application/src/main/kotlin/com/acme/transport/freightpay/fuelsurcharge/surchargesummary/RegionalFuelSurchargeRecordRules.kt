package com.acme.transport.freightpay.fuelsurcharge.surchargesummary

/** A rule a regional fuel surcharge record must pass. Check returns its problems, or none. */
interface RegionalFuelSurchargeRecordRule {
    fun check(item: RegionalFuelSurchargeRecord): MutableList<String>
}

data class RegionalFuelSurchargeRecordRequiredCarrierRule(
    var message: String,
) : RegionalFuelSurchargeRecordRule {
    override fun check(item: RegionalFuelSurchargeRecord): MutableList<String> {
        if (item.carrier.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalFuelSurchargeRecordCodePrefixRule(var prefix: String) : RegionalFuelSurchargeRecordRule {
    override fun check(item: RegionalFuelSurchargeRecord): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RegionalFuelSurchargeRecordRequiredNameRule(var message: String) : RegionalFuelSurchargeRecordRule {
    override fun check(item: RegionalFuelSurchargeRecord): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalFuelSurchargeRecordCostRangeRule(
    var min: Double,
    var max: Double,
) : RegionalFuelSurchargeRecordRule {
    override fun check(item: RegionalFuelSurchargeRecord): MutableList<String> {
        val value = item.cost
        if (value < min || value > max) {
            return mutableListOf<String>("cost $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RegionalFuelSurchargeRecordRequiredCodeRule(var message: String) : RegionalFuelSurchargeRecordRule {
    override fun check(item: RegionalFuelSurchargeRecord): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional fuel surcharge record. */
data class RegionalFuelSurchargeRecordValidator(var rules: MutableList<RegionalFuelSurchargeRecordRule>) {
    fun validate(item: RegionalFuelSurchargeRecord): MutableList<String> {
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

    fun requireValid(item: RegionalFuelSurchargeRecord) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalFuelSurchargeRecord>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalFuelSurchargeRecordValidator(): RegionalFuelSurchargeRecordValidator {
    return RegionalFuelSurchargeRecordValidator(
        mutableListOf<RegionalFuelSurchargeRecordRule>(
            RegionalFuelSurchargeRecordRequiredCarrierRule("regional fuel surcharge record needs this field"),
            RegionalFuelSurchargeRecordCodePrefixRule("X"),
            RegionalFuelSurchargeRecordRequiredNameRule("regional fuel surcharge record needs a value"),
            RegionalFuelSurchargeRecordCostRangeRule(7.3, 457.0),
            RegionalFuelSurchargeRecordRequiredCodeRule("regional fuel surcharge record needs a value"),
        ),
    )
}
