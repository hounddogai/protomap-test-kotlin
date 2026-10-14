package com.acme.transport.lastmile.locker.group

import com.acme.transport.kit.text.normalize

/** A rule a locker record must pass. Check returns its problems, or none. */
interface LockerRecordRule {
    fun check(item: LockerRecord): MutableList<String>
}

data class LockerRecordDiscountRangeRule(var min: Double, var max: Double) : LockerRecordRule {
    override fun check(item: LockerRecord): MutableList<String> {
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

data class LockerRecordGraceDaysRangeRule(var min: Int, var max: Int) : LockerRecordRule {
    override fun check(item: LockerRecord): MutableList<String> {
        val value = item.graceDays
        if (value < min || value > max) {
            return mutableListOf<String>("grace days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LockerRecordExternalRefLengthRule(var maxLength: Int) : LockerRecordRule {
    override fun check(item: LockerRecord): MutableList<String> {
        val size = item.externalRef.length
        if (size > maxLength) {
            return mutableListOf<String>("external ref is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LockerRecordEtaMinutesRangeRule(var min: Int, var max: Int) : LockerRecordRule {
    override fun check(item: LockerRecord): MutableList<String> {
        val value = item.etaMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("eta minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LockerRecordRequiredCodeRule(var message: String) : LockerRecordRule {
    override fun check(item: LockerRecord): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LockerRecordAllowedVehicleIdRule(var allowed: MutableList<String>) : LockerRecordRule {
    override fun check(item: LockerRecord): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.vehicleId))) {
            return mutableListOf<String>("vehicle id ${item.vehicleId} is blocked")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a locker record. */
data class LockerRecordValidator(var rules: MutableList<LockerRecordRule>) {
    fun validate(item: LockerRecord): MutableList<String> {
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

    fun requireValid(item: LockerRecord) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LockerRecord>): MutableMap<String, MutableList<String>> {
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

fun defaultLockerRecordValidator(): LockerRecordValidator {
    return LockerRecordValidator(
        mutableListOf<LockerRecordRule>(
            LockerRecordDiscountRangeRule(0.7, 960.6),
            LockerRecordGraceDaysRangeRule(64, 3600),
            LockerRecordExternalRefLengthRule(64),
            LockerRecordEtaMinutesRangeRule(16, 32),
            LockerRecordRequiredCodeRule("locker record needs a value"),
            LockerRecordAllowedVehicleIdRule(mutableListOf<String>("manual")),
        ),
    )
}
