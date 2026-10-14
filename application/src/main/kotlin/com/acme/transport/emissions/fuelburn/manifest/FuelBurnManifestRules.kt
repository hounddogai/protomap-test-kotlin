package com.acme.transport.emissions.fuelburn.manifest

import com.acme.transport.kit.text.normalize

/** A rule a fuel burn manifest must pass. Check returns its problems, or none. */
interface FuelBurnManifestRule {
    fun check(item: FuelBurnManifest): MutableList<String>
}

data class FuelBurnManifestArchivedGuardRule(var limit: Int) : FuelBurnManifestRule {
    override fun check(item: FuelBurnManifest): MutableList<String> {
        if (item.archived && item.attempts > limit) {
            return mutableListOf<String>("attempts ${item.attempts} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

data class FuelBurnManifestRequiredVehicleIdRule(var message: String) : FuelBurnManifestRule {
    override fun check(item: FuelBurnManifest): MutableList<String> {
        if (item.vehicleId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FuelBurnManifestAttemptsRangeRule(var min: Int, var max: Int) : FuelBurnManifestRule {
    override fun check(item: FuelBurnManifest): MutableList<String> {
        val value = item.attempts
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("attempts $value is below $min")
        }
        if (value > max) {
            problems.add("attempts $value is above $max")
        }
        return problems
    }
}

data class FuelBurnManifestVehicleIdLengthRule(var maxLength: Int) : FuelBurnManifestRule {
    override fun check(item: FuelBurnManifest): MutableList<String> {
        val size = item.vehicleId.length
        if (size > maxLength) {
            return mutableListOf<String>("vehicle id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FuelBurnManifestAllowedLabelRule(var allowed: MutableList<String>) : FuelBurnManifestRule {
    override fun check(item: FuelBurnManifest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is too long")
        }
        return mutableListOf<String>()
    }
}

data class FuelBurnManifestCodePrefixRule(var prefix: String) : FuelBurnManifestRule {
    override fun check(item: FuelBurnManifest): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fuel burn manifest. */
data class FuelBurnManifestValidator(var rules: MutableList<FuelBurnManifestRule>) {
    fun validate(item: FuelBurnManifest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FuelBurnManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FuelBurnManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultFuelBurnManifestValidator(): FuelBurnManifestValidator {
    return FuelBurnManifestValidator(
        mutableListOf<FuelBurnManifestRule>(
            FuelBurnManifestArchivedGuardRule(120),
            FuelBurnManifestRequiredVehicleIdRule("fuel burn manifest needs a value"),
            FuelBurnManifestAttemptsRangeRule(16, 1000),
            FuelBurnManifestVehicleIdLengthRule(1000),
            FuelBurnManifestAllowedLabelRule(mutableListOf<String>("default")),
            FuelBurnManifestCodePrefixRule("ACM"),
        ),
    )
}
