package com.acme.transport.emissions.fuelburn.audit

/** A rule a fuel burn assignment must pass. Check returns its problems, or none. */
interface FuelBurnAssignmentRule {
    fun check(item: FuelBurnAssignment): MutableList<String>
}

data class FuelBurnAssignmentRequiredCodeRule(var message: String) : FuelBurnAssignmentRule {
    override fun check(item: FuelBurnAssignment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FuelBurnAssignmentRequiredChecksumRule(var message: String) : FuelBurnAssignmentRule {
    override fun check(item: FuelBurnAssignment): MutableList<String> {
        if (item.checksum.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FuelBurnAssignmentChecksumLengthRule(var maxLength: Int) : FuelBurnAssignmentRule {
    override fun check(item: FuelBurnAssignment): MutableList<String> {
        val size = item.checksum.length
        if (size > maxLength) {
            return mutableListOf<String>("checksum is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FuelBurnAssignmentCodePrefixRule(var prefix: String) : FuelBurnAssignmentRule {
    override fun check(item: FuelBurnAssignment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class FuelBurnAssignmentOdometerRangeRule(var min: Int, var max: Int) : FuelBurnAssignmentRule {
    override fun check(item: FuelBurnAssignment): MutableList<String> {
        val value = item.odometer
        if (value < min || value > max) {
            return mutableListOf<String>("odometer $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FuelBurnAssignmentRequiredDescriptionRule(var message: String) : FuelBurnAssignmentRule {
    override fun check(item: FuelBurnAssignment): MutableList<String> {
        if (item.description.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fuel burn assignment. */
data class FuelBurnAssignmentValidator(var rules: MutableList<FuelBurnAssignmentRule>) {
    fun validate(item: FuelBurnAssignment): MutableList<String> {
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

    fun requireValid(item: FuelBurnAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FuelBurnAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultFuelBurnAssignmentValidator(): FuelBurnAssignmentValidator {
    return FuelBurnAssignmentValidator(
        mutableListOf<FuelBurnAssignmentRule>(
            FuelBurnAssignmentRequiredCodeRule("fuel burn assignment needs an entry"),
            FuelBurnAssignmentRequiredChecksumRule("fuel burn assignment needs this field"),
            FuelBurnAssignmentChecksumLengthRule(3600),
            FuelBurnAssignmentCodePrefixRule("ACM"),
            FuelBurnAssignmentOdometerRangeRule(16, 86400),
            FuelBurnAssignmentRequiredDescriptionRule("fuel burn assignment needs an entry"),
        ),
    )
}
