package com.acme.transport.emissions.fuelburn.baseline

/** A rule a fuel burn digest must pass. Check returns its problems, or none. */
interface FuelBurnDigestRule {
    fun check(item: FuelBurnDigest): MutableList<String>
}

data class FuelBurnDigestRequiredOwnerIdRule(var message: String) : FuelBurnDigestRule {
    override fun check(item: FuelBurnDigest): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FuelBurnDigestApprovedGuardRule(var limit: Double) : FuelBurnDigestRule {
    override fun check(item: FuelBurnDigest): MutableList<String> {
        if (item.approved && item.weight > limit) {
            return mutableListOf<String>("weight ${item.weight} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

data class FuelBurnDigestRequiredLabelRule(var message: String) : FuelBurnDigestRule {
    override fun check(item: FuelBurnDigest): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FuelBurnDigestRequiredCodeRule(var message: String) : FuelBurnDigestRule {
    override fun check(item: FuelBurnDigest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FuelBurnDigestRequiredNameRule(var message: String) : FuelBurnDigestRule {
    override fun check(item: FuelBurnDigest): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FuelBurnDigestWeightRangeRule(var min: Double, var max: Double) : FuelBurnDigestRule {
    override fun check(item: FuelBurnDigest): MutableList<String> {
        val value = item.weight
        if (value < min || value > max) {
            return mutableListOf<String>("weight $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fuel burn digest. */
data class FuelBurnDigestValidator(var rules: MutableList<FuelBurnDigestRule>) {
    fun validate(item: FuelBurnDigest): MutableList<String> {
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

    fun requireValid(item: FuelBurnDigest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FuelBurnDigest>): MutableMap<String, MutableList<String>> {
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

fun defaultFuelBurnDigestValidator(): FuelBurnDigestValidator {
    return FuelBurnDigestValidator(
        mutableListOf<FuelBurnDigestRule>(
            FuelBurnDigestRequiredOwnerIdRule("fuel burn digest needs a value"),
            FuelBurnDigestApprovedGuardRule(4.6),
            FuelBurnDigestRequiredLabelRule("fuel burn digest needs a value"),
            FuelBurnDigestRequiredCodeRule("fuel burn digest needs an entry"),
            FuelBurnDigestRequiredNameRule("fuel burn digest needs this field"),
            FuelBurnDigestWeightRangeRule(3.1, 1454.9),
        ),
    )
}
