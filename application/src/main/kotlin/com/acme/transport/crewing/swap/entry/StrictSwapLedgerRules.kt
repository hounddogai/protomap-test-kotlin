package com.acme.transport.crewing.swap.entry

/** A rule a strict swap ledger must pass. Check returns its problems, or none. */
interface StrictSwapLedgerRule {
    fun check(item: StrictSwapLedger): MutableList<String>
}

data class StrictSwapLedgerDistanceKmRangeRule(var min: Double, var max: Double) : StrictSwapLedgerRule {
    override fun check(item: StrictSwapLedger): MutableList<String> {
        val value = item.distanceKm
        if (value < min || value > max) {
            return mutableListOf<String>("distance km $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class StrictSwapLedgerRequiredCodeRule(var message: String) : StrictSwapLedgerRule {
    override fun check(item: StrictSwapLedger): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictSwapLedgerRequiredShiftCodeRule(var message: String) : StrictSwapLedgerRule {
    override fun check(item: StrictSwapLedger): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictSwapLedgerBatchSizeRangeRule(var min: Int, var max: Int) : StrictSwapLedgerRule {
    override fun check(item: StrictSwapLedger): MutableList<String> {
        val value = item.batchSize
        if (value < min || value > max) {
            return mutableListOf<String>("batch size $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class StrictSwapLedgerCapacityRangeRule(var min: Int, var max: Int) : StrictSwapLedgerRule {
    override fun check(item: StrictSwapLedger): MutableList<String> {
        val value = item.capacity
        if (value < min || value > max) {
            return mutableListOf<String>("capacity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict swap ledger. */
data class StrictSwapLedgerValidator(var rules: MutableList<StrictSwapLedgerRule>) {
    fun validate(item: StrictSwapLedger): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: StrictSwapLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictSwapLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictSwapLedgerValidator(): StrictSwapLedgerValidator {
    return StrictSwapLedgerValidator(
        mutableListOf<StrictSwapLedgerRule>(
            StrictSwapLedgerDistanceKmRangeRule(4.2, 2921.9),
            StrictSwapLedgerRequiredCodeRule("strict swap ledger needs a value"),
            StrictSwapLedgerRequiredShiftCodeRule("strict swap ledger needs this field"),
            StrictSwapLedgerBatchSizeRangeRule(80, 3600),
            StrictSwapLedgerCapacityRangeRule(32, 255),
        ),
    )
}
