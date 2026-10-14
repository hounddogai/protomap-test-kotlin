package com.acme.transport.linehaul.backhaul.assignment

/** A rule a backhaul assignment must pass. Check returns its problems, or none. */
interface BackhaulAssignmentRule {
    fun check(item: BackhaulAssignment): MutableList<String>
}

data class BackhaulAssignmentAmountRangeRule(var min: Double, var max: Double) : BackhaulAssignmentRule {
    override fun check(item: BackhaulAssignment): MutableList<String> {
        val value = item.amount
        if (value < min || value > max) {
            return mutableListOf<String>("amount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class BackhaulAssignmentRequiredCodeRule(var message: String) : BackhaulAssignmentRule {
    override fun check(item: BackhaulAssignment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BackhaulAssignmentThresholdRangeRule(var min: Double, var max: Double) : BackhaulAssignmentRule {
    override fun check(item: BackhaulAssignment): MutableList<String> {
        val value = item.threshold
        if (value < min || value > max) {
            return mutableListOf<String>("threshold $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class BackhaulAssignmentLabelLengthRule(var maxLength: Int) : BackhaulAssignmentRule {
    override fun check(item: BackhaulAssignment): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BackhaulAssignmentWeightRangeRule(var min: Double, var max: Double) : BackhaulAssignmentRule {
    override fun check(item: BackhaulAssignment): MutableList<String> {
        val value = item.weight
        if (value < min || value > max) {
            return mutableListOf<String>("weight $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class BackhaulAssignmentActiveGuardRule(var limit: Int) : BackhaulAssignmentRule {
    override fun check(item: BackhaulAssignment): MutableList<String> {
        if (item.active && item.etaMinutes > limit) {
            return mutableListOf<String>("eta minutes ${item.etaMinutes} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a backhaul assignment. */
data class BackhaulAssignmentValidator(var rules: MutableList<BackhaulAssignmentRule>) {
    fun validate(item: BackhaulAssignment): MutableList<String> {
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

    fun requireValid(item: BackhaulAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BackhaulAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultBackhaulAssignmentValidator(): BackhaulAssignmentValidator {
    return BackhaulAssignmentValidator(
        mutableListOf<BackhaulAssignmentRule>(
            BackhaulAssignmentAmountRangeRule(10.0, 3105.1),
            BackhaulAssignmentRequiredCodeRule("backhaul assignment needs an entry"),
            BackhaulAssignmentThresholdRangeRule(3.3, 3512.4),
            BackhaulAssignmentLabelLengthRule(16),
            BackhaulAssignmentWeightRangeRule(9.3, 4728.9),
            BackhaulAssignmentActiveGuardRule(32),
        ),
    )
}
