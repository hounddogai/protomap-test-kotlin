package com.acme.transport.customs.hold.slot

/** A rule a hold manifest must pass. Check returns its problems, or none. */
interface HoldManifestRule {
    fun check(item: HoldManifest): MutableList<String>
}

data class HoldManifestRequiredNameRule(var message: String) : HoldManifestRule {
    override fun check(item: HoldManifest): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class HoldManifestWeightRangeRule(var min: Double, var max: Double) : HoldManifestRule {
    override fun check(item: HoldManifest): MutableList<String> {
        val value = item.weight
        if (value < min || value > max) {
            return mutableListOf<String>("weight $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class HoldManifestFuelLitersRangeRule(var min: Double, var max: Double) : HoldManifestRule {
    override fun check(item: HoldManifest): MutableList<String> {
        val value = item.fuelLiters
        if (value < min || value > max) {
            return mutableListOf<String>("fuel liters $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a hold manifest. */
data class HoldManifestValidator(var rules: MutableList<HoldManifestRule>) {
    fun validate(item: HoldManifest): MutableList<String> {
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

    fun requireValid(item: HoldManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<HoldManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultHoldManifestValidator(): HoldManifestValidator {
    return HoldManifestValidator(
        mutableListOf<HoldManifestRule>(
            HoldManifestRequiredNameRule("hold manifest needs a value"),
            HoldManifestWeightRangeRule(4.8, 469.0),
            HoldManifestFuelLitersRangeRule(6.1, 2386.0),
        ),
    )
}
