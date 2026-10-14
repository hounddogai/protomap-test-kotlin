package com.acme.transport.fleetops.leasereturn.forecast

/** A rule a lease return manifest must pass. Check returns its problems, or none. */
interface LeaseReturnManifestRule {
    fun check(item: LeaseReturnManifest): MutableList<String>
}

data class LeaseReturnManifestRateRangeRule(var min: Double, var max: Double) : LeaseReturnManifestRule {
    override fun check(item: LeaseReturnManifest): MutableList<String> {
        val value = item.rate
        if (value < min || value > max) {
            return mutableListOf<String>("rate $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LeaseReturnManifestPhoneLengthRule(var maxLength: Int) : LeaseReturnManifestRule {
    override fun check(item: LeaseReturnManifest): MutableList<String> {
        val size = item.phone.length
        if (size > maxLength) {
            return mutableListOf<String>("phone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LeaseReturnManifestFuelLitersRangeRule(var min: Double, var max: Double) : LeaseReturnManifestRule {
    override fun check(item: LeaseReturnManifest): MutableList<String> {
        val value = item.fuelLiters
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("fuel liters $value is below $min")
        }
        if (value > max) {
            problems.add("fuel liters $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a lease return manifest. */
data class LeaseReturnManifestValidator(var rules: MutableList<LeaseReturnManifestRule>) {
    fun validate(item: LeaseReturnManifest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LeaseReturnManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LeaseReturnManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultLeaseReturnManifestValidator(): LeaseReturnManifestValidator {
    return LeaseReturnManifestValidator(
        mutableListOf<LeaseReturnManifestRule>(
            LeaseReturnManifestRateRangeRule(7.8, 4556.1),
            LeaseReturnManifestPhoneLengthRule(32),
            LeaseReturnManifestFuelLitersRangeRule(5.9, 1515.8),
        ),
    )
}
