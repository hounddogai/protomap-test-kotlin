package com.acme.transport.emissions.routeoffset.assignment

/** A rule a route offset assignment must pass. Check returns its problems, or none. */
interface RouteOffsetAssignmentRule {
    fun check(item: RouteOffsetAssignment): MutableList<String>
}

data class RouteOffsetAssignmentUpdatedAtRangeRule(var min: Int, var max: Int) : RouteOffsetAssignmentRule {
    override fun check(item: RouteOffsetAssignment): MutableList<String> {
        val value = item.updatedAt
        if (value < min || value > max) {
            return mutableListOf<String>("updated at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetAssignmentRequiredRouteCodeRule(var message: String) : RouteOffsetAssignmentRule {
    override fun check(item: RouteOffsetAssignment): MutableList<String> {
        if (item.routeCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetAssignmentRequiredLabelRule(var message: String) : RouteOffsetAssignmentRule {
    override fun check(item: RouteOffsetAssignment): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetAssignmentRequiredVehicleIdRule(var message: String) : RouteOffsetAssignmentRule {
    override fun check(item: RouteOffsetAssignment): MutableList<String> {
        if (item.vehicleId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetAssignmentRouteCodeLengthRule(var maxLength: Int) : RouteOffsetAssignmentRule {
    override fun check(item: RouteOffsetAssignment): MutableList<String> {
        val size = item.routeCode.length
        if (size > maxLength) {
            return mutableListOf<String>("route code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetAssignmentLabelLengthRule(var maxLength: Int) : RouteOffsetAssignmentRule {
    override fun check(item: RouteOffsetAssignment): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a route offset assignment. */
data class RouteOffsetAssignmentValidator(var rules: MutableList<RouteOffsetAssignmentRule>) {
    fun validate(item: RouteOffsetAssignment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RouteOffsetAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RouteOffsetAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultRouteOffsetAssignmentValidator(): RouteOffsetAssignmentValidator {
    return RouteOffsetAssignmentValidator(
        mutableListOf<RouteOffsetAssignmentRule>(
            RouteOffsetAssignmentUpdatedAtRangeRule(255, 255),
            RouteOffsetAssignmentRequiredRouteCodeRule("route offset assignment needs a value"),
            RouteOffsetAssignmentRequiredLabelRule("route offset assignment needs this field"),
            RouteOffsetAssignmentRequiredVehicleIdRule("route offset assignment needs a value"),
            RouteOffsetAssignmentRouteCodeLengthRule(80),
            RouteOffsetAssignmentLabelLengthRule(120),
        ),
    )
}
