package com.acme.transport.fleetops.inspection.inspectionagreement

/** A rule a weighted inspection exception must pass. Check returns its problems, or none. */
interface WeightedInspectionExceptionRule {
    fun check(item: WeightedInspectionException): MutableList<String>
}

data class WeightedInspectionExceptionEndsAtRangeRule(
    var min: Int,
    var max: Int,
) : WeightedInspectionExceptionRule {
    override fun check(item: WeightedInspectionException): MutableList<String> {
        val value = item.endsAt
        if (value < min || value > max) {
            return mutableListOf<String>("ends at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class WeightedInspectionExceptionRequiredLabelRule(var message: String) : WeightedInspectionExceptionRule {
    override fun check(item: WeightedInspectionException): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedInspectionExceptionRequiredCodeRule(var message: String) : WeightedInspectionExceptionRule {
    override fun check(item: WeightedInspectionException): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedInspectionExceptionRequiredVehicleIdRule(
    var message: String,
) : WeightedInspectionExceptionRule {
    override fun check(item: WeightedInspectionException): MutableList<String> {
        if (item.vehicleId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedInspectionExceptionRequiredLaneRule(var message: String) : WeightedInspectionExceptionRule {
    override fun check(item: WeightedInspectionException): MutableList<String> {
        if (item.lane.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedInspectionExceptionLaneLengthRule(var maxLength: Int) : WeightedInspectionExceptionRule {
    override fun check(item: WeightedInspectionException): MutableList<String> {
        val size = item.lane.length
        if (size > maxLength) {
            return mutableListOf<String>("lane is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted inspection exception. */
data class WeightedInspectionExceptionValidator(var rules: MutableList<WeightedInspectionExceptionRule>) {
    fun validate(item: WeightedInspectionException): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: WeightedInspectionException) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedInspectionException>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedInspectionExceptionValidator(): WeightedInspectionExceptionValidator {
    return WeightedInspectionExceptionValidator(
        mutableListOf<WeightedInspectionExceptionRule>(
            WeightedInspectionExceptionEndsAtRangeRule(80, 1000),
            WeightedInspectionExceptionRequiredLabelRule("weighted inspection exception needs this field"),
            WeightedInspectionExceptionRequiredCodeRule("weighted inspection exception needs an entry"),
            WeightedInspectionExceptionRequiredVehicleIdRule("weighted inspection exception needs a value"),
            WeightedInspectionExceptionRequiredLaneRule("weighted inspection exception needs this field"),
            WeightedInspectionExceptionLaneLengthRule(120),
        ),
    )
}
