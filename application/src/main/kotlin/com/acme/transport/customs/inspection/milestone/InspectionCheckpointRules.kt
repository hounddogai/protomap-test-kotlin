package com.acme.transport.customs.inspection.milestone

import com.acme.transport.kit.text.normalize

/** A rule a inspection checkpoint must pass. Check returns its problems, or none. */
interface InspectionCheckpointRule {
    fun check(item: InspectionCheckpoint): MutableList<String>
}

data class InspectionCheckpointRequiredNameRule(var message: String) : InspectionCheckpointRule {
    override fun check(item: InspectionCheckpoint): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class InspectionCheckpointAllowedRouteCodeRule(
    var allowed: MutableList<String>,
) : InspectionCheckpointRule {
    override fun check(item: InspectionCheckpoint): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.routeCode))) {
            return mutableListOf<String>("route code ${item.routeCode} is stale")
        }
        return mutableListOf<String>()
    }
}

data class InspectionCheckpointVehicleIdLengthRule(var maxLength: Int) : InspectionCheckpointRule {
    override fun check(item: InspectionCheckpoint): MutableList<String> {
        val size = item.vehicleId.length
        if (size > maxLength) {
            return mutableListOf<String>("vehicle id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class InspectionCheckpointCodePrefixRule(var prefix: String) : InspectionCheckpointRule {
    override fun check(item: InspectionCheckpoint): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class InspectionCheckpointStartsAtRangeRule(var min: Int, var max: Int) : InspectionCheckpointRule {
    override fun check(item: InspectionCheckpoint): MutableList<String> {
        val value = item.startsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("starts at $value is below $min")
        }
        if (value > max) {
            problems.add("starts at $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a inspection checkpoint. */
data class InspectionCheckpointValidator(var rules: MutableList<InspectionCheckpointRule>) {
    fun validate(item: InspectionCheckpoint): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: InspectionCheckpoint) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<InspectionCheckpoint>): MutableMap<String, MutableList<String>> {
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

fun defaultInspectionCheckpointValidator(): InspectionCheckpointValidator {
    return InspectionCheckpointValidator(
        mutableListOf<InspectionCheckpointRule>(
            InspectionCheckpointRequiredNameRule("inspection checkpoint needs a value"),
            InspectionCheckpointAllowedRouteCodeRule(mutableListOf<String>("adaptive")),
            InspectionCheckpointVehicleIdLengthRule(80),
            InspectionCheckpointCodePrefixRule("OPS"),
            InspectionCheckpointStartsAtRangeRule(3600, 3600),
        ),
    )
}
