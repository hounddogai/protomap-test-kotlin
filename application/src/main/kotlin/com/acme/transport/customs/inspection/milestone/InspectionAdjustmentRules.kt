package com.acme.transport.customs.inspection.milestone

/** A rule a inspection adjustment must pass. Check returns its problems, or none. */
interface InspectionAdjustmentRule {
    fun check(item: InspectionAdjustment): MutableList<String>
}

data class InspectionAdjustmentEndsAtRangeRule(var min: Int, var max: Int) : InspectionAdjustmentRule {
    override fun check(item: InspectionAdjustment): MutableList<String> {
        val value = item.endsAt
        if (value < min || value > max) {
            return mutableListOf<String>("ends at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class InspectionAdjustmentCodePrefixRule(var prefix: String) : InspectionAdjustmentRule {
    override fun check(item: InspectionAdjustment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class InspectionAdjustmentGraceDaysRangeRule(var min: Int, var max: Int) : InspectionAdjustmentRule {
    override fun check(item: InspectionAdjustment): MutableList<String> {
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

data class InspectionAdjustmentOdometerRangeRule(var min: Int, var max: Int) : InspectionAdjustmentRule {
    override fun check(item: InspectionAdjustment): MutableList<String> {
        val value = item.odometer
        if (value < min || value > max) {
            return mutableListOf<String>("odometer $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class InspectionAdjustmentRequiredCategoryRule(var message: String) : InspectionAdjustmentRule {
    override fun check(item: InspectionAdjustment): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class InspectionAdjustmentCategoryLengthRule(var maxLength: Int) : InspectionAdjustmentRule {
    override fun check(item: InspectionAdjustment): MutableList<String> {
        val size = item.category.length
        if (size > maxLength) {
            return mutableListOf<String>("category is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a inspection adjustment. */
data class InspectionAdjustmentValidator(var rules: MutableList<InspectionAdjustmentRule>) {
    fun validate(item: InspectionAdjustment): MutableList<String> {
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

    fun requireValid(item: InspectionAdjustment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<InspectionAdjustment>): MutableMap<String, MutableList<String>> {
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

fun defaultInspectionAdjustmentValidator(): InspectionAdjustmentValidator {
    return InspectionAdjustmentValidator(
        mutableListOf<InspectionAdjustmentRule>(
            InspectionAdjustmentEndsAtRangeRule(1000, 1000),
            InspectionAdjustmentCodePrefixRule("OPS"),
            InspectionAdjustmentGraceDaysRangeRule(64, 255),
            InspectionAdjustmentOdometerRangeRule(120, 255),
            InspectionAdjustmentRequiredCategoryRule("inspection adjustment needs a value"),
            InspectionAdjustmentCategoryLengthRule(32),
        ),
    )
}
