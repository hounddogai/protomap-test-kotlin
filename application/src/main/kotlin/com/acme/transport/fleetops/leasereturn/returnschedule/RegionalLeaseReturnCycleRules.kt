package com.acme.transport.fleetops.leasereturn.returnschedule

/** A rule a regional lease return cycle must pass. Check returns its problems, or none. */
interface RegionalLeaseReturnCycleRule {
    fun check(item: RegionalLeaseReturnCycle): MutableList<String>
}

data class RegionalLeaseReturnCycleRequiredLabelRule(var message: String) : RegionalLeaseReturnCycleRule {
    override fun check(item: RegionalLeaseReturnCycle): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalLeaseReturnCycleQuantityRangeRule(
    var min: Int,
    var max: Int,
) : RegionalLeaseReturnCycleRule {
    override fun check(item: RegionalLeaseReturnCycle): MutableList<String> {
        val value = item.quantity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("quantity $value is below $min")
        }
        if (value > max) {
            problems.add("quantity $value is above $max")
        }
        return problems
    }
}

data class RegionalLeaseReturnCycleUnitLengthRule(var maxLength: Int) : RegionalLeaseReturnCycleRule {
    override fun check(item: RegionalLeaseReturnCycle): MutableList<String> {
        val size = item.unit.length
        if (size > maxLength) {
            return mutableListOf<String>("unit is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalLeaseReturnCycleCodeLengthRule(var maxLength: Int) : RegionalLeaseReturnCycleRule {
    override fun check(item: RegionalLeaseReturnCycle): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalLeaseReturnCycleRequiredUnitRule(var message: String) : RegionalLeaseReturnCycleRule {
    override fun check(item: RegionalLeaseReturnCycle): MutableList<String> {
        if (item.unit.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional lease return cycle. */
data class RegionalLeaseReturnCycleValidator(var rules: MutableList<RegionalLeaseReturnCycleRule>) {
    fun validate(item: RegionalLeaseReturnCycle): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RegionalLeaseReturnCycle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalLeaseReturnCycle>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalLeaseReturnCycleValidator(): RegionalLeaseReturnCycleValidator {
    return RegionalLeaseReturnCycleValidator(
        mutableListOf<RegionalLeaseReturnCycleRule>(
            RegionalLeaseReturnCycleRequiredLabelRule("regional lease return cycle needs a value"),
            RegionalLeaseReturnCycleQuantityRangeRule(16, 80),
            RegionalLeaseReturnCycleUnitLengthRule(16),
            RegionalLeaseReturnCycleCodeLengthRule(16),
            RegionalLeaseReturnCycleRequiredUnitRule("regional lease return cycle needs a value"),
        ),
    )
}
