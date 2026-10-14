package com.acme.transport.fleetops.leasereturn.recorditem

/** A rule a legacy lease return digest must pass. Check returns its problems, or none. */
interface LegacyLeaseReturnDigestRule {
    fun check(item: LegacyLeaseReturnDigest): MutableList<String>
}

data class LegacyLeaseReturnDigestRatioRangeRule(
    var min: Double,
    var max: Double,
) : LegacyLeaseReturnDigestRule {
    override fun check(item: LegacyLeaseReturnDigest): MutableList<String> {
        val value = item.ratio
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ratio $value is below $min")
        }
        if (value > max) {
            problems.add("ratio $value is above $max")
        }
        return problems
    }
}

data class LegacyLeaseReturnDigestDockLengthRule(var maxLength: Int) : LegacyLeaseReturnDigestRule {
    override fun check(item: LegacyLeaseReturnDigest): MutableList<String> {
        val size = item.dock.length
        if (size > maxLength) {
            return mutableListOf<String>("dock is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyLeaseReturnDigestNameLengthRule(var maxLength: Int) : LegacyLeaseReturnDigestRule {
    override fun check(item: LegacyLeaseReturnDigest): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyLeaseReturnDigestRequiredVehicleIdRule(var message: String) : LegacyLeaseReturnDigestRule {
    override fun check(item: LegacyLeaseReturnDigest): MutableList<String> {
        if (item.vehicleId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyLeaseReturnDigestRequiredNameRule(var message: String) : LegacyLeaseReturnDigestRule {
    override fun check(item: LegacyLeaseReturnDigest): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy lease return digest. */
data class LegacyLeaseReturnDigestValidator(var rules: MutableList<LegacyLeaseReturnDigestRule>) {
    fun validate(item: LegacyLeaseReturnDigest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LegacyLeaseReturnDigest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyLeaseReturnDigest>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyLeaseReturnDigestValidator(): LegacyLeaseReturnDigestValidator {
    return LegacyLeaseReturnDigestValidator(
        mutableListOf<LegacyLeaseReturnDigestRule>(
            LegacyLeaseReturnDigestRatioRangeRule(9.6, 2598.5),
            LegacyLeaseReturnDigestDockLengthRule(255),
            LegacyLeaseReturnDigestNameLengthRule(32),
            LegacyLeaseReturnDigestRequiredVehicleIdRule("legacy lease return digest needs an entry"),
            LegacyLeaseReturnDigestRequiredNameRule("legacy lease return digest needs a value"),
        ),
    )
}
