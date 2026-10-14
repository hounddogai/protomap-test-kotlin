package com.acme.transport.fleetops.leasereturn.returnentry

/** A rule a legacy lease return entry must pass. Check returns its problems, or none. */
interface LegacyLeaseReturnEntryRule {
    fun check(item: LegacyLeaseReturnEntry): MutableList<String>
}

data class LegacyLeaseReturnEntryVehicleIdLengthRule(var maxLength: Int) : LegacyLeaseReturnEntryRule {
    override fun check(item: LegacyLeaseReturnEntry): MutableList<String> {
        val size = item.vehicleId.length
        if (size > maxLength) {
            return mutableListOf<String>("vehicle id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyLeaseReturnEntryDiscountRangeRule(
    var min: Double,
    var max: Double,
) : LegacyLeaseReturnEntryRule {
    override fun check(item: LegacyLeaseReturnEntry): MutableList<String> {
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

data class LegacyLeaseReturnEntryRequiredChecksumRule(var message: String) : LegacyLeaseReturnEntryRule {
    override fun check(item: LegacyLeaseReturnEntry): MutableList<String> {
        if (item.checksum.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyLeaseReturnEntryArchivedGuardRule(var limit: Double) : LegacyLeaseReturnEntryRule {
    override fun check(item: LegacyLeaseReturnEntry): MutableList<String> {
        if (item.archived && item.discount > limit) {
            return mutableListOf<String>("discount ${item.discount} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

data class LegacyLeaseReturnEntryCodeLengthRule(var maxLength: Int) : LegacyLeaseReturnEntryRule {
    override fun check(item: LegacyLeaseReturnEntry): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyLeaseReturnEntryRequiredVehicleIdRule(var message: String) : LegacyLeaseReturnEntryRule {
    override fun check(item: LegacyLeaseReturnEntry): MutableList<String> {
        if (item.vehicleId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy lease return entry. */
data class LegacyLeaseReturnEntryValidator(var rules: MutableList<LegacyLeaseReturnEntryRule>) {
    fun validate(item: LegacyLeaseReturnEntry): MutableList<String> {
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

    fun requireValid(item: LegacyLeaseReturnEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyLeaseReturnEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyLeaseReturnEntryValidator(): LegacyLeaseReturnEntryValidator {
    return LegacyLeaseReturnEntryValidator(
        mutableListOf<LegacyLeaseReturnEntryRule>(
            LegacyLeaseReturnEntryVehicleIdLengthRule(80),
            LegacyLeaseReturnEntryDiscountRangeRule(9.6, 3778.4),
            LegacyLeaseReturnEntryRequiredChecksumRule("legacy lease return entry needs an entry"),
            LegacyLeaseReturnEntryArchivedGuardRule(7.2),
            LegacyLeaseReturnEntryCodeLengthRule(16),
            LegacyLeaseReturnEntryRequiredVehicleIdRule("legacy lease return entry needs a value"),
        ),
    )
}
