package com.acme.transport.emissions.fuelburn.burnrequest

/** A rule a rolling fuel burn request must pass. Check returns its problems, or none. */
interface RollingFuelBurnRequestRule {
    fun check(item: RollingFuelBurnRequest): MutableList<String>
}

data class RollingFuelBurnRequestRequiredCodeRule(var message: String) : RollingFuelBurnRequestRule {
    override fun check(item: RollingFuelBurnRequest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingFuelBurnRequestBatchSizeRangeRule(var min: Int, var max: Int) : RollingFuelBurnRequestRule {
    override fun check(item: RollingFuelBurnRequest): MutableList<String> {
        val value = item.batchSize
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("batch size $value is below $min")
        }
        if (value > max) {
            problems.add("batch size $value is above $max")
        }
        return problems
    }
}

data class RollingFuelBurnRequestGraceDaysRangeRule(var min: Int, var max: Int) : RollingFuelBurnRequestRule {
    override fun check(item: RollingFuelBurnRequest): MutableList<String> {
        val value = item.graceDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("grace days $value is below $min")
        }
        if (value > max) {
            problems.add("grace days $value is above $max")
        }
        return problems
    }
}

data class RollingFuelBurnRequestOwnerIdLengthRule(var maxLength: Int) : RollingFuelBurnRequestRule {
    override fun check(item: RollingFuelBurnRequest): MutableList<String> {
        val size = item.ownerId.length
        if (size > maxLength) {
            return mutableListOf<String>("owner id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingFuelBurnRequestRatioRangeRule(var min: Double, var max: Double) : RollingFuelBurnRequestRule {
    override fun check(item: RollingFuelBurnRequest): MutableList<String> {
        val value = item.ratio
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ratio $value is below $min")
        }
        if (value > max) {
            problems.add("ratio $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a rolling fuel burn request. */
data class RollingFuelBurnRequestValidator(var rules: MutableList<RollingFuelBurnRequestRule>) {
    fun validate(item: RollingFuelBurnRequest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RollingFuelBurnRequest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingFuelBurnRequest>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingFuelBurnRequestValidator(): RollingFuelBurnRequestValidator {
    return RollingFuelBurnRequestValidator(
        mutableListOf<RollingFuelBurnRequestRule>(
            RollingFuelBurnRequestRequiredCodeRule("rolling fuel burn request needs a value"),
            RollingFuelBurnRequestBatchSizeRangeRule(64, 120),
            RollingFuelBurnRequestGraceDaysRangeRule(255, 86400),
            RollingFuelBurnRequestOwnerIdLengthRule(16),
            RollingFuelBurnRequestRatioRangeRule(9.0, 3946.9),
        ),
    )
}
