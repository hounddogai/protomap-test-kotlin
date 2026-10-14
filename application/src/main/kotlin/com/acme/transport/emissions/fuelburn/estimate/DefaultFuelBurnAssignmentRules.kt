package com.acme.transport.emissions.fuelburn.estimate

/** A rule a default fuel burn assignment must pass. Check returns its problems, or none. */
interface DefaultFuelBurnAssignmentRule {
    fun check(item: DefaultFuelBurnAssignment): MutableList<String>
}

data class DefaultFuelBurnAssignmentRequiredExternalRefRule(
    var message: String,
) : DefaultFuelBurnAssignmentRule {
    override fun check(item: DefaultFuelBurnAssignment): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultFuelBurnAssignmentMinimumRangeRule(
    var min: Double,
    var max: Double,
) : DefaultFuelBurnAssignmentRule {
    override fun check(item: DefaultFuelBurnAssignment): MutableList<String> {
        val value = item.minimum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("minimum $value is below $min")
        }
        if (value > max) {
            problems.add("minimum $value is above $max")
        }
        return problems
    }
}

data class DefaultFuelBurnAssignmentCodeLengthRule(var maxLength: Int) : DefaultFuelBurnAssignmentRule {
    override fun check(item: DefaultFuelBurnAssignment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultFuelBurnAssignmentFuelLitersRangeRule(
    var min: Double,
    var max: Double,
) : DefaultFuelBurnAssignmentRule {
    override fun check(item: DefaultFuelBurnAssignment): MutableList<String> {
        val value = item.fuelLiters
        if (value < min || value > max) {
            return mutableListOf<String>("fuel liters $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DefaultFuelBurnAssignmentRequiredTitleRule(var message: String) : DefaultFuelBurnAssignmentRule {
    override fun check(item: DefaultFuelBurnAssignment): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultFuelBurnAssignmentLaneLengthRule(var maxLength: Int) : DefaultFuelBurnAssignmentRule {
    override fun check(item: DefaultFuelBurnAssignment): MutableList<String> {
        val size = item.lane.length
        if (size > maxLength) {
            return mutableListOf<String>("lane is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default fuel burn assignment. */
data class DefaultFuelBurnAssignmentValidator(var rules: MutableList<DefaultFuelBurnAssignmentRule>) {
    fun validate(item: DefaultFuelBurnAssignment): MutableList<String> {
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

    fun requireValid(item: DefaultFuelBurnAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultFuelBurnAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultFuelBurnAssignmentValidator(): DefaultFuelBurnAssignmentValidator {
    return DefaultFuelBurnAssignmentValidator(
        mutableListOf<DefaultFuelBurnAssignmentRule>(
            DefaultFuelBurnAssignmentRequiredExternalRefRule("default fuel burn assignment needs this field"),
            DefaultFuelBurnAssignmentMinimumRangeRule(2.3, 728.3),
            DefaultFuelBurnAssignmentCodeLengthRule(64),
            DefaultFuelBurnAssignmentFuelLitersRangeRule(9.9, 1795.2),
            DefaultFuelBurnAssignmentRequiredTitleRule("default fuel burn assignment needs this field"),
            DefaultFuelBurnAssignmentLaneLengthRule(1000),
        ),
    )
}
