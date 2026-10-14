package com.acme.transport.safety.cameraevent.adjustment

/** A rule a fallback camera event adjustment must pass. Check returns its problems, or none. */
interface FallbackCameraEventAdjustmentRule {
    fun check(item: FallbackCameraEventAdjustment): MutableList<String>
}

data class FallbackCameraEventAdjustmentDiscountRangeRule(
    var min: Double,
    var max: Double,
) : FallbackCameraEventAdjustmentRule {
    override fun check(item: FallbackCameraEventAdjustment): MutableList<String> {
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

data class FallbackCameraEventAdjustmentCeilingRangeRule(
    var min: Double,
    var max: Double,
) : FallbackCameraEventAdjustmentRule {
    override fun check(item: FallbackCameraEventAdjustment): MutableList<String> {
        val value = item.ceiling
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ceiling $value is below $min")
        }
        if (value > max) {
            problems.add("ceiling $value is above $max")
        }
        return problems
    }
}

data class FallbackCameraEventAdjustmentApprovedGuardRule(
    var limit: Double,
) : FallbackCameraEventAdjustmentRule {
    override fun check(item: FallbackCameraEventAdjustment): MutableList<String> {
        if (item.approved && item.distanceKm > limit) {
            return mutableListOf<String>("distance km ${item.distanceKm} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback camera event adjustment. */
data class FallbackCameraEventAdjustmentValidator(var rules: MutableList<FallbackCameraEventAdjustmentRule>) {
    fun validate(item: FallbackCameraEventAdjustment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FallbackCameraEventAdjustment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackCameraEventAdjustment>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackCameraEventAdjustmentValidator(): FallbackCameraEventAdjustmentValidator {
    return FallbackCameraEventAdjustmentValidator(
        mutableListOf<FallbackCameraEventAdjustmentRule>(
            FallbackCameraEventAdjustmentDiscountRangeRule(3.4, 2576.4),
            FallbackCameraEventAdjustmentCeilingRangeRule(2.1, 3751.1),
            FallbackCameraEventAdjustmentApprovedGuardRule(3.3),
        ),
    )
}
