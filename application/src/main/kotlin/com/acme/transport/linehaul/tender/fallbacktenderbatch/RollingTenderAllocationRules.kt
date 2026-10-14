package com.acme.transport.linehaul.tender.fallbacktenderbatch

/** A rule a rolling tender allocation must pass. Check returns its problems, or none. */
interface RollingTenderAllocationRule {
    fun check(item: RollingTenderAllocation): MutableList<String>
}

data class RollingTenderAllocationFuelLitersRangeRule(
    var min: Double,
    var max: Double,
) : RollingTenderAllocationRule {
    override fun check(item: RollingTenderAllocation): MutableList<String> {
        val value = item.fuelLiters
        if (value < min || value > max) {
            return mutableListOf<String>("fuel liters $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RollingTenderAllocationRequiredLabelRule(var message: String) : RollingTenderAllocationRule {
    override fun check(item: RollingTenderAllocation): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingTenderAllocationLabelLengthRule(var maxLength: Int) : RollingTenderAllocationRule {
    override fun check(item: RollingTenderAllocation): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling tender allocation. */
data class RollingTenderAllocationValidator(var rules: MutableList<RollingTenderAllocationRule>) {
    fun validate(item: RollingTenderAllocation): MutableList<String> {
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

    fun requireValid(item: RollingTenderAllocation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingTenderAllocation>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingTenderAllocationValidator(): RollingTenderAllocationValidator {
    return RollingTenderAllocationValidator(
        mutableListOf<RollingTenderAllocationRule>(
            RollingTenderAllocationFuelLitersRangeRule(4.0, 1903.9),
            RollingTenderAllocationRequiredLabelRule("rolling tender allocation needs this field"),
            RollingTenderAllocationLabelLengthRule(3600),
        ),
    )
}
