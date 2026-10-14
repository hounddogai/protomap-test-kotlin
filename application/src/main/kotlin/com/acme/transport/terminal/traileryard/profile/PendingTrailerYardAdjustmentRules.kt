package com.acme.transport.terminal.traileryard.profile

/** A rule a pending trailer yard adjustment must pass. Check returns its problems, or none. */
interface PendingTrailerYardAdjustmentRule {
    fun check(item: PendingTrailerYardAdjustment): MutableList<String>
}

data class PendingTrailerYardAdjustmentCodePrefixRule(var prefix: String) : PendingTrailerYardAdjustmentRule {
    override fun check(item: PendingTrailerYardAdjustment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class PendingTrailerYardAdjustmentActiveGuardRule(var limit: Int) : PendingTrailerYardAdjustmentRule {
    override fun check(item: PendingTrailerYardAdjustment): MutableList<String> {
        if (item.active && item.odometer > limit) {
            return mutableListOf<String>("odometer ${item.odometer} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

data class PendingTrailerYardAdjustmentCostRangeRule(
    var min: Double,
    var max: Double,
) : PendingTrailerYardAdjustmentRule {
    override fun check(item: PendingTrailerYardAdjustment): MutableList<String> {
        val value = item.cost
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("cost $value is below $min")
        }
        if (value > max) {
            problems.add("cost $value is above $max")
        }
        return problems
    }
}

data class PendingTrailerYardAdjustmentRequiredReviewerRule(
    var message: String,
) : PendingTrailerYardAdjustmentRule {
    override fun check(item: PendingTrailerYardAdjustment): MutableList<String> {
        if (item.reviewer.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingTrailerYardAdjustmentRequiredCodeRule(
    var message: String,
) : PendingTrailerYardAdjustmentRule {
    override fun check(item: PendingTrailerYardAdjustment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending trailer yard adjustment. */
data class PendingTrailerYardAdjustmentValidator(var rules: MutableList<PendingTrailerYardAdjustmentRule>) {
    fun validate(item: PendingTrailerYardAdjustment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PendingTrailerYardAdjustment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingTrailerYardAdjustment>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingTrailerYardAdjustmentValidator(): PendingTrailerYardAdjustmentValidator {
    return PendingTrailerYardAdjustmentValidator(
        mutableListOf<PendingTrailerYardAdjustmentRule>(
            PendingTrailerYardAdjustmentCodePrefixRule("Q"),
            PendingTrailerYardAdjustmentActiveGuardRule(120),
            PendingTrailerYardAdjustmentCostRangeRule(8.5, 2145.2),
            PendingTrailerYardAdjustmentRequiredReviewerRule("pending trailer yard adjustment needs a value"),
            PendingTrailerYardAdjustmentRequiredCodeRule("pending trailer yard adjustment needs this field"),
        ),
    )
}
