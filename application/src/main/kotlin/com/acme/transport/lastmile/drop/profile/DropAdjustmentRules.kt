package com.acme.transport.lastmile.drop.profile

/** A rule a drop adjustment must pass. Check returns its problems, or none. */
interface DropAdjustmentRule {
    fun check(item: DropAdjustment): MutableList<String>
}

data class DropAdjustmentTitleLengthRule(var maxLength: Int) : DropAdjustmentRule {
    override fun check(item: DropAdjustment): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DropAdjustmentStopCountRangeRule(var min: Int, var max: Int) : DropAdjustmentRule {
    override fun check(item: DropAdjustment): MutableList<String> {
        val value = item.stopCount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("stop count $value is below $min")
        }
        if (value > max) {
            problems.add("stop count $value is above $max")
        }
        return problems
    }
}

data class DropAdjustmentFloorValueRangeRule(var min: Double, var max: Double) : DropAdjustmentRule {
    override fun check(item: DropAdjustment): MutableList<String> {
        val value = item.floorValue
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("floor value $value is below $min")
        }
        if (value > max) {
            problems.add("floor value $value is above $max")
        }
        return problems
    }
}

data class DropAdjustmentRequiredSummaryRule(var message: String) : DropAdjustmentRule {
    override fun check(item: DropAdjustment): MutableList<String> {
        if (item.summary.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DropAdjustmentRequiredCodeRule(var message: String) : DropAdjustmentRule {
    override fun check(item: DropAdjustment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DropAdjustmentPriorityRangeRule(var min: Int, var max: Int) : DropAdjustmentRule {
    override fun check(item: DropAdjustment): MutableList<String> {
        val value = item.priority
        if (value < min || value > max) {
            return mutableListOf<String>("priority $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a drop adjustment. */
data class DropAdjustmentValidator(var rules: MutableList<DropAdjustmentRule>) {
    fun validate(item: DropAdjustment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DropAdjustment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DropAdjustment>): MutableMap<String, MutableList<String>> {
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

fun defaultDropAdjustmentValidator(): DropAdjustmentValidator {
    return DropAdjustmentValidator(
        mutableListOf<DropAdjustmentRule>(
            DropAdjustmentTitleLengthRule(16),
            DropAdjustmentStopCountRangeRule(32, 3600),
            DropAdjustmentFloorValueRangeRule(8.3, 1753.4),
            DropAdjustmentRequiredSummaryRule("drop adjustment needs this field"),
            DropAdjustmentRequiredCodeRule("drop adjustment needs this field"),
            DropAdjustmentPriorityRangeRule(64, 255),
        ),
    )
}
