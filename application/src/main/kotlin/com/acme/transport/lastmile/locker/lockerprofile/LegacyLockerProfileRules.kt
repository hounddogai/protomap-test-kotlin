package com.acme.transport.lastmile.locker.lockerprofile

/** A rule a legacy locker profile must pass. Check returns its problems, or none. */
interface LegacyLockerProfileRule {
    fun check(item: LegacyLockerProfile): MutableList<String>
}

data class LegacyLockerProfilePalletsRangeRule(var min: Int, var max: Int) : LegacyLockerProfileRule {
    override fun check(item: LegacyLockerProfile): MutableList<String> {
        val value = item.pallets
        if (value < min || value > max) {
            return mutableListOf<String>("pallets $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LegacyLockerProfileUpdatedAtRangeRule(var min: Int, var max: Int) : LegacyLockerProfileRule {
    override fun check(item: LegacyLockerProfile): MutableList<String> {
        val value = item.updatedAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("updated at $value is below $min")
        }
        if (value > max) {
            problems.add("updated at $value is above $max")
        }
        return problems
    }
}

data class LegacyLockerProfileDriverIdLengthRule(var maxLength: Int) : LegacyLockerProfileRule {
    override fun check(item: LegacyLockerProfile): MutableList<String> {
        val size = item.driverId.length
        if (size > maxLength) {
            return mutableListOf<String>("driver id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyLockerProfileRequiredNameRule(var message: String) : LegacyLockerProfileRule {
    override fun check(item: LegacyLockerProfile): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyLockerProfileDiscountRangeRule(var min: Double, var max: Double) : LegacyLockerProfileRule {
    override fun check(item: LegacyLockerProfile): MutableList<String> {
        val value = item.discount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("discount $value is below $min")
        }
        if (value > max) {
            problems.add("discount $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a legacy locker profile. */
data class LegacyLockerProfileValidator(var rules: MutableList<LegacyLockerProfileRule>) {
    fun validate(item: LegacyLockerProfile): MutableList<String> {
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

    fun requireValid(item: LegacyLockerProfile) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyLockerProfile>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyLockerProfileValidator(): LegacyLockerProfileValidator {
    return LegacyLockerProfileValidator(
        mutableListOf<LegacyLockerProfileRule>(
            LegacyLockerProfilePalletsRangeRule(120, 120),
            LegacyLockerProfileUpdatedAtRangeRule(16, 64),
            LegacyLockerProfileDriverIdLengthRule(1000),
            LegacyLockerProfileRequiredNameRule("legacy locker profile needs this field"),
            LegacyLockerProfileDiscountRangeRule(9.6, 4970.0),
        ),
    )
}
