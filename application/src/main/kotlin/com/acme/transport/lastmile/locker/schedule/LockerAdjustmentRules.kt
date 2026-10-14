package com.acme.transport.lastmile.locker.schedule

/** A rule a locker adjustment must pass. Check returns its problems, or none. */
interface LockerAdjustmentRule {
    fun check(item: LockerAdjustment): MutableList<String>
}

data class LockerAdjustmentStartsAtRangeRule(var min: Int, var max: Int) : LockerAdjustmentRule {
    override fun check(item: LockerAdjustment): MutableList<String> {
        val value = item.startsAt
        if (value < min || value > max) {
            return mutableListOf<String>("starts at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LockerAdjustmentEndsAtRangeRule(var min: Int, var max: Int) : LockerAdjustmentRule {
    override fun check(item: LockerAdjustment): MutableList<String> {
        val value = item.endsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ends at $value is below $min")
        }
        if (value > max) {
            problems.add("ends at $value is above $max")
        }
        return problems
    }
}

data class LockerAdjustmentQuantityRangeRule(var min: Int, var max: Int) : LockerAdjustmentRule {
    override fun check(item: LockerAdjustment): MutableList<String> {
        val value = item.quantity
        if (value < min || value > max) {
            return mutableListOf<String>("quantity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LockerAdjustmentRequiredVehicleIdRule(var message: String) : LockerAdjustmentRule {
    override fun check(item: LockerAdjustment): MutableList<String> {
        if (item.vehicleId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LockerAdjustmentRequiredContactEmailRule(var message: String) : LockerAdjustmentRule {
    override fun check(item: LockerAdjustment): MutableList<String> {
        if (item.contactEmail.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a locker adjustment. */
data class LockerAdjustmentValidator(var rules: MutableList<LockerAdjustmentRule>) {
    fun validate(item: LockerAdjustment): MutableList<String> {
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

    fun requireValid(item: LockerAdjustment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LockerAdjustment>): MutableMap<String, MutableList<String>> {
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

fun defaultLockerAdjustmentValidator(): LockerAdjustmentValidator {
    return LockerAdjustmentValidator(
        mutableListOf<LockerAdjustmentRule>(
            LockerAdjustmentStartsAtRangeRule(80, 3600),
            LockerAdjustmentEndsAtRangeRule(120, 3600),
            LockerAdjustmentQuantityRangeRule(80, 86400),
            LockerAdjustmentRequiredVehicleIdRule("locker adjustment needs an entry"),
            LockerAdjustmentRequiredContactEmailRule("locker adjustment needs a value"),
        ),
    )
}
