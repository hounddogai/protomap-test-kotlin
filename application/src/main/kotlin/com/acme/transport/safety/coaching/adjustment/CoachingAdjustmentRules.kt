package com.acme.transport.safety.coaching.adjustment

/** A rule a coaching adjustment must pass. Check returns its problems, or none. */
interface CoachingAdjustmentRule {
    fun check(item: CoachingAdjustment): MutableList<String>
}

data class CoachingAdjustmentRequiredCodeRule(var message: String) : CoachingAdjustmentRule {
    override fun check(item: CoachingAdjustment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CoachingAdjustmentRequiredSourceRule(var message: String) : CoachingAdjustmentRule {
    override fun check(item: CoachingAdjustment): MutableList<String> {
        if (item.source.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CoachingAdjustmentPalletsRangeRule(var min: Int, var max: Int) : CoachingAdjustmentRule {
    override fun check(item: CoachingAdjustment): MutableList<String> {
        val value = item.pallets
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("pallets $value is below $min")
        }
        if (value > max) {
            problems.add("pallets $value is above $max")
        }
        return problems
    }
}

data class CoachingAdjustmentThresholdRangeRule(var min: Double, var max: Double) : CoachingAdjustmentRule {
    override fun check(item: CoachingAdjustment): MutableList<String> {
        val value = item.threshold
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("threshold $value is below $min")
        }
        if (value > max) {
            problems.add("threshold $value is above $max")
        }
        return problems
    }
}

data class CoachingAdjustmentRequiredNameRule(var message: String) : CoachingAdjustmentRule {
    override fun check(item: CoachingAdjustment): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CoachingAdjustmentCodeLengthRule(var maxLength: Int) : CoachingAdjustmentRule {
    override fun check(item: CoachingAdjustment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a coaching adjustment. */
data class CoachingAdjustmentValidator(var rules: MutableList<CoachingAdjustmentRule>) {
    fun validate(item: CoachingAdjustment): MutableList<String> {
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

    fun requireValid(item: CoachingAdjustment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CoachingAdjustment>): MutableMap<String, MutableList<String>> {
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

fun defaultCoachingAdjustmentValidator(): CoachingAdjustmentValidator {
    return CoachingAdjustmentValidator(
        mutableListOf<CoachingAdjustmentRule>(
            CoachingAdjustmentRequiredCodeRule("coaching adjustment needs this field"),
            CoachingAdjustmentRequiredSourceRule("coaching adjustment needs this field"),
            CoachingAdjustmentPalletsRangeRule(32, 255),
            CoachingAdjustmentThresholdRangeRule(0.2, 4668.8),
            CoachingAdjustmentRequiredNameRule("coaching adjustment needs a value"),
            CoachingAdjustmentCodeLengthRule(64),
        ),
    )
}
