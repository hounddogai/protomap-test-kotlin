package com.acme.transport.fleetops.utilization.utilizationvariance

/** A rule a global utilization claim must pass. Check returns its problems, or none. */
interface GlobalUtilizationClaimRule {
    fun check(item: GlobalUtilizationClaim): MutableList<String>
}

data class GlobalUtilizationClaimBatchSizeRangeRule(var min: Int, var max: Int) : GlobalUtilizationClaimRule {
    override fun check(item: GlobalUtilizationClaim): MutableList<String> {
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

data class GlobalUtilizationClaimFuelLitersRangeRule(
    var min: Double,
    var max: Double,
) : GlobalUtilizationClaimRule {
    override fun check(item: GlobalUtilizationClaim): MutableList<String> {
        val value = item.fuelLiters
        if (value < min || value > max) {
            return mutableListOf<String>("fuel liters $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class GlobalUtilizationClaimRequiredTitleRule(var message: String) : GlobalUtilizationClaimRule {
    override fun check(item: GlobalUtilizationClaim): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global utilization claim. */
data class GlobalUtilizationClaimValidator(var rules: MutableList<GlobalUtilizationClaimRule>) {
    fun validate(item: GlobalUtilizationClaim): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: GlobalUtilizationClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalUtilizationClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalUtilizationClaimValidator(): GlobalUtilizationClaimValidator {
    return GlobalUtilizationClaimValidator(
        mutableListOf<GlobalUtilizationClaimRule>(
            GlobalUtilizationClaimBatchSizeRangeRule(32, 32),
            GlobalUtilizationClaimFuelLitersRangeRule(4.4, 4149.4),
            GlobalUtilizationClaimRequiredTitleRule("global utilization claim needs a value"),
        ),
    )
}
