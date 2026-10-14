package com.acme.transport.network.node.template

/** A rule a default node assignment must pass. Check returns its problems, or none. */
interface DefaultNodeAssignmentRule {
    fun check(item: DefaultNodeAssignment): MutableList<String>
}

data class DefaultNodeAssignmentCodeLengthRule(var maxLength: Int) : DefaultNodeAssignmentRule {
    override fun check(item: DefaultNodeAssignment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultNodeAssignmentVehicleIdLengthRule(var maxLength: Int) : DefaultNodeAssignmentRule {
    override fun check(item: DefaultNodeAssignment): MutableList<String> {
        val size = item.vehicleId.length
        if (size > maxLength) {
            return mutableListOf<String>("vehicle id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultNodeAssignmentRequiredDescriptionRule(var message: String) : DefaultNodeAssignmentRule {
    override fun check(item: DefaultNodeAssignment): MutableList<String> {
        if (item.description.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultNodeAssignmentRequiredCodeRule(var message: String) : DefaultNodeAssignmentRule {
    override fun check(item: DefaultNodeAssignment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultNodeAssignmentFuelLitersRangeRule(
    var min: Double,
    var max: Double,
) : DefaultNodeAssignmentRule {
    override fun check(item: DefaultNodeAssignment): MutableList<String> {
        val value = item.fuelLiters
        if (value < min || value > max) {
            return mutableListOf<String>("fuel liters $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DefaultNodeAssignmentDescriptionLengthRule(var maxLength: Int) : DefaultNodeAssignmentRule {
    override fun check(item: DefaultNodeAssignment): MutableList<String> {
        val size = item.description.length
        if (size > maxLength) {
            return mutableListOf<String>("description is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default node assignment. */
data class DefaultNodeAssignmentValidator(var rules: MutableList<DefaultNodeAssignmentRule>) {
    fun validate(item: DefaultNodeAssignment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefaultNodeAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultNodeAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultNodeAssignmentValidator(): DefaultNodeAssignmentValidator {
    return DefaultNodeAssignmentValidator(
        mutableListOf<DefaultNodeAssignmentRule>(
            DefaultNodeAssignmentCodeLengthRule(1000),
            DefaultNodeAssignmentVehicleIdLengthRule(32),
            DefaultNodeAssignmentRequiredDescriptionRule("default node assignment needs a value"),
            DefaultNodeAssignmentRequiredCodeRule("default node assignment needs a value"),
            DefaultNodeAssignmentFuelLitersRangeRule(2.9, 151.2),
            DefaultNodeAssignmentDescriptionLengthRule(1000),
        ),
    )
}
