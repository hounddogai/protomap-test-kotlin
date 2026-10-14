package com.acme.transport.crewing.restbreak.breakentry

/** A rule a fallback rest break exception must pass. Check returns its problems, or none. */
interface FallbackRestBreakExceptionRule {
    fun check(item: FallbackRestBreakException): MutableList<String>
}

data class FallbackRestBreakExceptionRequiredOwnerIdRule(var message: String) : FallbackRestBreakExceptionRule {
    override fun check(item: FallbackRestBreakException): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackRestBreakExceptionApprovedGuardRule(var limit: Int) : FallbackRestBreakExceptionRule {
    override fun check(item: FallbackRestBreakException): MutableList<String> {
        if (item.approved && item.quantity > limit) {
            return mutableListOf<String>("quantity ${item.quantity} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

data class FallbackRestBreakExceptionQuantityRangeRule(
    var min: Int,
    var max: Int,
) : FallbackRestBreakExceptionRule {
    override fun check(item: FallbackRestBreakException): MutableList<String> {
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

data class FallbackRestBreakExceptionStartsAtRangeRule(
    var min: Int,
    var max: Int,
) : FallbackRestBreakExceptionRule {
    override fun check(item: FallbackRestBreakException): MutableList<String> {
        val value = item.startsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("starts at $value is below $min")
        }
        if (value > max) {
            problems.add("starts at $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a fallback rest break exception. */
data class FallbackRestBreakExceptionValidator(var rules: MutableList<FallbackRestBreakExceptionRule>) {
    fun validate(item: FallbackRestBreakException): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FallbackRestBreakException) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackRestBreakException>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackRestBreakExceptionValidator(): FallbackRestBreakExceptionValidator {
    return FallbackRestBreakExceptionValidator(
        mutableListOf<FallbackRestBreakExceptionRule>(
            FallbackRestBreakExceptionRequiredOwnerIdRule("fallback rest break exception needs this field"),
            FallbackRestBreakExceptionApprovedGuardRule(80),
            FallbackRestBreakExceptionQuantityRangeRule(80, 80),
            FallbackRestBreakExceptionStartsAtRangeRule(16, 32),
        ),
    )
}
