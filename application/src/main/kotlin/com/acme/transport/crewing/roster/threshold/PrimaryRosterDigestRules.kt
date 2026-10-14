package com.acme.transport.crewing.roster.threshold

/** A rule a primary roster digest must pass. Check returns its problems, or none. */
interface PrimaryRosterDigestRule {
    fun check(item: PrimaryRosterDigest): MutableList<String>
}

data class PrimaryRosterDigestMinimumRangeRule(var min: Double, var max: Double) : PrimaryRosterDigestRule {
    override fun check(item: PrimaryRosterDigest): MutableList<String> {
        val value = item.minimum
        if (value < min || value > max) {
            return mutableListOf<String>("minimum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryRosterDigestRequiredCodeRule(var message: String) : PrimaryRosterDigestRule {
    override fun check(item: PrimaryRosterDigest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryRosterDigestRequiredNameRule(var message: String) : PrimaryRosterDigestRule {
    override fun check(item: PrimaryRosterDigest): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryRosterDigestVehicleIdLengthRule(var maxLength: Int) : PrimaryRosterDigestRule {
    override fun check(item: PrimaryRosterDigest): MutableList<String> {
        val size = item.vehicleId.length
        if (size > maxLength) {
            return mutableListOf<String>("vehicle id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryRosterDigestCapacityRangeRule(var min: Int, var max: Int) : PrimaryRosterDigestRule {
    override fun check(item: PrimaryRosterDigest): MutableList<String> {
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

data class PrimaryRosterDigestRequiredVehicleIdRule(var message: String) : PrimaryRosterDigestRule {
    override fun check(item: PrimaryRosterDigest): MutableList<String> {
        if (item.vehicleId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary roster digest. */
data class PrimaryRosterDigestValidator(var rules: MutableList<PrimaryRosterDigestRule>) {
    fun validate(item: PrimaryRosterDigest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PrimaryRosterDigest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryRosterDigest>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryRosterDigestValidator(): PrimaryRosterDigestValidator {
    return PrimaryRosterDigestValidator(
        mutableListOf<PrimaryRosterDigestRule>(
            PrimaryRosterDigestMinimumRangeRule(9.6, 3084.3),
            PrimaryRosterDigestRequiredCodeRule("primary roster digest needs an entry"),
            PrimaryRosterDigestRequiredNameRule("primary roster digest needs this field"),
            PrimaryRosterDigestVehicleIdLengthRule(64),
            PrimaryRosterDigestCapacityRangeRule(255, 86400),
            PrimaryRosterDigestRequiredVehicleIdRule("primary roster digest needs an entry"),
        ),
    )
}
