package com.acme.transport.freightpay.fuelsurcharge.slot

/** A rule a fuel surcharge manifest must pass. Check returns its problems, or none. */
interface FuelSurchargeManifestRule {
    fun check(item: FuelSurchargeManifest): MutableList<String>
}

data class FuelSurchargeManifestRequiredCodeRule(var message: String) : FuelSurchargeManifestRule {
    override fun check(item: FuelSurchargeManifest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FuelSurchargeManifestWeightRangeRule(var min: Double, var max: Double) : FuelSurchargeManifestRule {
    override fun check(item: FuelSurchargeManifest): MutableList<String> {
        val value = item.weight
        if (value < min || value > max) {
            return mutableListOf<String>("weight $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FuelSurchargeManifestCodePrefixRule(var prefix: String) : FuelSurchargeManifestRule {
    override fun check(item: FuelSurchargeManifest): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class FuelSurchargeManifestRequiredVehicleIdRule(var message: String) : FuelSurchargeManifestRule {
    override fun check(item: FuelSurchargeManifest): MutableList<String> {
        if (item.vehicleId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FuelSurchargeManifestStopCountRangeRule(var min: Int, var max: Int) : FuelSurchargeManifestRule {
    override fun check(item: FuelSurchargeManifest): MutableList<String> {
        val value = item.stopCount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("stop count $value is below $min")
        }
        if (value > max) {
            problems.add("stop count $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a fuel surcharge manifest. */
data class FuelSurchargeManifestValidator(var rules: MutableList<FuelSurchargeManifestRule>) {
    fun validate(item: FuelSurchargeManifest): MutableList<String> {
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

    fun requireValid(item: FuelSurchargeManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FuelSurchargeManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultFuelSurchargeManifestValidator(): FuelSurchargeManifestValidator {
    return FuelSurchargeManifestValidator(
        mutableListOf<FuelSurchargeManifestRule>(
            FuelSurchargeManifestRequiredCodeRule("fuel surcharge manifest needs an entry"),
            FuelSurchargeManifestWeightRangeRule(8.5, 1710.0),
            FuelSurchargeManifestCodePrefixRule("REF"),
            FuelSurchargeManifestRequiredVehicleIdRule("fuel surcharge manifest needs an entry"),
            FuelSurchargeManifestStopCountRangeRule(64, 64),
        ),
    )
}
