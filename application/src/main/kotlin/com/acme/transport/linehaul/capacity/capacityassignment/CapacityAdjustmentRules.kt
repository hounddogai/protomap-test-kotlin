package com.acme.transport.linehaul.capacity.capacityassignment

/** A rule a capacity adjustment must pass. Check returns its problems, or none. */
interface CapacityAdjustmentRule {
    fun check(item: CapacityAdjustment): MutableList<String>
}

data class CapacityAdjustmentTitleLengthRule(var maxLength: Int) : CapacityAdjustmentRule {
    override fun check(item: CapacityAdjustment): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CapacityAdjustmentRevisionRangeRule(var min: Int, var max: Int) : CapacityAdjustmentRule {
    override fun check(item: CapacityAdjustment): MutableList<String> {
        val value = item.revision
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("revision $value is below $min")
        }
        if (value > max) {
            problems.add("revision $value is above $max")
        }
        return problems
    }
}

data class CapacityAdjustmentRequiredTitleRule(var message: String) : CapacityAdjustmentRule {
    override fun check(item: CapacityAdjustment): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CapacityAdjustmentRequiredCodeRule(var message: String) : CapacityAdjustmentRule {
    override fun check(item: CapacityAdjustment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CapacityAdjustmentRequiredTrailerIdRule(var message: String) : CapacityAdjustmentRule {
    override fun check(item: CapacityAdjustment): MutableList<String> {
        if (item.trailerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capacity adjustment. */
data class CapacityAdjustmentValidator(var rules: MutableList<CapacityAdjustmentRule>) {
    fun validate(item: CapacityAdjustment): MutableList<String> {
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

    fun requireValid(item: CapacityAdjustment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CapacityAdjustment>): MutableMap<String, MutableList<String>> {
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

fun defaultCapacityAdjustmentValidator(): CapacityAdjustmentValidator {
    return CapacityAdjustmentValidator(
        mutableListOf<CapacityAdjustmentRule>(
            CapacityAdjustmentTitleLengthRule(80),
            CapacityAdjustmentRevisionRangeRule(32, 3600),
            CapacityAdjustmentRequiredTitleRule("capacity adjustment needs this field"),
            CapacityAdjustmentRequiredCodeRule("capacity adjustment needs this field"),
            CapacityAdjustmentRequiredTrailerIdRule("capacity adjustment needs a value"),
        ),
    )
}
