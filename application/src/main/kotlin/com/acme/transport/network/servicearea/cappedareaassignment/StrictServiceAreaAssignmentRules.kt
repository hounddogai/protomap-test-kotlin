package com.acme.transport.network.servicearea.cappedareaassignment

/** A rule a strict service area assignment must pass. Check returns its problems, or none. */
interface StrictServiceAreaAssignmentRule {
    fun check(item: StrictServiceAreaAssignment): MutableList<String>
}

data class StrictServiceAreaAssignmentRequiredNameRule(var message: String) : StrictServiceAreaAssignmentRule {
    override fun check(item: StrictServiceAreaAssignment): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictServiceAreaAssignmentStopCountRangeRule(
    var min: Int,
    var max: Int,
) : StrictServiceAreaAssignmentRule {
    override fun check(item: StrictServiceAreaAssignment): MutableList<String> {
        val value = item.stopCount
        if (value < min || value > max) {
            return mutableListOf<String>("stop count $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class StrictServiceAreaAssignmentNameLengthRule(var maxLength: Int) : StrictServiceAreaAssignmentRule {
    override fun check(item: StrictServiceAreaAssignment): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictServiceAreaAssignmentCapacityRangeRule(
    var min: Int,
    var max: Int,
) : StrictServiceAreaAssignmentRule {
    override fun check(item: StrictServiceAreaAssignment): MutableList<String> {
        val value = item.capacity
        if (value < min || value > max) {
            return mutableListOf<String>("capacity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class StrictServiceAreaAssignmentCodePrefixRule(var prefix: String) : StrictServiceAreaAssignmentRule {
    override fun check(item: StrictServiceAreaAssignment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class StrictServiceAreaAssignmentCarrierLengthRule(var maxLength: Int) : StrictServiceAreaAssignmentRule {
    override fun check(item: StrictServiceAreaAssignment): MutableList<String> {
        val size = item.carrier.length
        if (size > maxLength) {
            return mutableListOf<String>("carrier is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict service area assignment. */
data class StrictServiceAreaAssignmentValidator(var rules: MutableList<StrictServiceAreaAssignmentRule>) {
    fun validate(item: StrictServiceAreaAssignment): MutableList<String> {
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

    fun requireValid(item: StrictServiceAreaAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictServiceAreaAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictServiceAreaAssignmentValidator(): StrictServiceAreaAssignmentValidator {
    return StrictServiceAreaAssignmentValidator(
        mutableListOf<StrictServiceAreaAssignmentRule>(
            StrictServiceAreaAssignmentRequiredNameRule("strict service area assignment needs this field"),
            StrictServiceAreaAssignmentStopCountRangeRule(32, 255),
            StrictServiceAreaAssignmentNameLengthRule(32),
            StrictServiceAreaAssignmentCapacityRangeRule(64, 86400),
            StrictServiceAreaAssignmentCodePrefixRule("ACM"),
            StrictServiceAreaAssignmentCarrierLengthRule(1000),
        ),
    )
}
