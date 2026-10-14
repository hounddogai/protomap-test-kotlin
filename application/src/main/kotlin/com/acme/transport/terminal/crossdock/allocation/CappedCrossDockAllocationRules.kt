package com.acme.transport.terminal.crossdock.allocation

/** A rule a capped cross dock allocation must pass. Check returns its problems, or none. */
interface CappedCrossDockAllocationRule {
    fun check(item: CappedCrossDockAllocation): MutableList<String>
}

data class CappedCrossDockAllocationCodeLengthRule(var maxLength: Int) : CappedCrossDockAllocationRule {
    override fun check(item: CappedCrossDockAllocation): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedCrossDockAllocationVehicleIdLengthRule(var maxLength: Int) : CappedCrossDockAllocationRule {
    override fun check(item: CappedCrossDockAllocation): MutableList<String> {
        val size = item.vehicleId.length
        if (size > maxLength) {
            return mutableListOf<String>("vehicle id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedCrossDockAllocationRequiredLabelRule(var message: String) : CappedCrossDockAllocationRule {
    override fun check(item: CappedCrossDockAllocation): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedCrossDockAllocationRequiredVehicleIdRule(var message: String) : CappedCrossDockAllocationRule {
    override fun check(item: CappedCrossDockAllocation): MutableList<String> {
        if (item.vehicleId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedCrossDockAllocationDiscountRangeRule(
    var min: Double,
    var max: Double,
) : CappedCrossDockAllocationRule {
    override fun check(item: CappedCrossDockAllocation): MutableList<String> {
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

/** Runs every rule of a capped cross dock allocation. */
data class CappedCrossDockAllocationValidator(var rules: MutableList<CappedCrossDockAllocationRule>) {
    fun validate(item: CappedCrossDockAllocation): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CappedCrossDockAllocation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedCrossDockAllocation>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedCrossDockAllocationValidator(): CappedCrossDockAllocationValidator {
    return CappedCrossDockAllocationValidator(
        mutableListOf<CappedCrossDockAllocationRule>(
            CappedCrossDockAllocationCodeLengthRule(80),
            CappedCrossDockAllocationVehicleIdLengthRule(3600),
            CappedCrossDockAllocationRequiredLabelRule("capped cross dock allocation needs this field"),
            CappedCrossDockAllocationRequiredVehicleIdRule("capped cross dock allocation needs an entry"),
            CappedCrossDockAllocationDiscountRangeRule(5.9, 642.4),
        ),
    )
}
