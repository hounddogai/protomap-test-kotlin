package com.acme.transport.lastmile.locker.agreement

/** A rule a locker entry must pass. Check returns its problems, or none. */
interface LockerEntryRule {
    fun check(item: LockerEntry): MutableList<String>
}

data class LockerEntryLabelLengthRule(var maxLength: Int) : LockerEntryRule {
    override fun check(item: LockerEntry): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LockerEntryMaximumRangeRule(var min: Double, var max: Double) : LockerEntryRule {
    override fun check(item: LockerEntry): MutableList<String> {
        val value = item.maximum
        if (value < min || value > max) {
            return mutableListOf<String>("maximum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LockerEntryVehicleIdLengthRule(var maxLength: Int) : LockerEntryRule {
    override fun check(item: LockerEntry): MutableList<String> {
        val size = item.vehicleId.length
        if (size > maxLength) {
            return mutableListOf<String>("vehicle id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a locker entry. */
data class LockerEntryValidator(var rules: MutableList<LockerEntryRule>) {
    fun validate(item: LockerEntry): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LockerEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LockerEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultLockerEntryValidator(): LockerEntryValidator {
    return LockerEntryValidator(
        mutableListOf<LockerEntryRule>(
            LockerEntryLabelLengthRule(255),
            LockerEntryMaximumRangeRule(4.5, 1185.8),
            LockerEntryVehicleIdLengthRule(3600),
        ),
    )
}
