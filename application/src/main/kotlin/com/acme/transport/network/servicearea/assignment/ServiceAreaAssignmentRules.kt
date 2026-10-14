package com.acme.transport.network.servicearea.assignment

/** A rule a service area assignment must pass. Check returns its problems, or none. */
interface ServiceAreaAssignmentRule {
    fun check(item: ServiceAreaAssignment): MutableList<String>
}

data class ServiceAreaAssignmentLabelLengthRule(var maxLength: Int) : ServiceAreaAssignmentRule {
    override fun check(item: ServiceAreaAssignment): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ServiceAreaAssignmentFuelLitersRangeRule(
    var min: Double,
    var max: Double,
) : ServiceAreaAssignmentRule {
    override fun check(item: ServiceAreaAssignment): MutableList<String> {
        val value = item.fuelLiters
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("fuel liters $value is below $min")
        }
        if (value > max) {
            problems.add("fuel liters $value is above $max")
        }
        return problems
    }
}

data class ServiceAreaAssignmentRequiredOwnerIdRule(var message: String) : ServiceAreaAssignmentRule {
    override fun check(item: ServiceAreaAssignment): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ServiceAreaAssignmentCodePrefixRule(var prefix: String) : ServiceAreaAssignmentRule {
    override fun check(item: ServiceAreaAssignment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a service area assignment. */
data class ServiceAreaAssignmentValidator(var rules: MutableList<ServiceAreaAssignmentRule>) {
    fun validate(item: ServiceAreaAssignment): MutableList<String> {
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

    fun requireValid(item: ServiceAreaAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ServiceAreaAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultServiceAreaAssignmentValidator(): ServiceAreaAssignmentValidator {
    return ServiceAreaAssignmentValidator(
        mutableListOf<ServiceAreaAssignmentRule>(
            ServiceAreaAssignmentLabelLengthRule(255),
            ServiceAreaAssignmentFuelLitersRangeRule(6.6, 348.7),
            ServiceAreaAssignmentRequiredOwnerIdRule("service area assignment needs this field"),
            ServiceAreaAssignmentCodePrefixRule("ACM"),
        ),
    )
}
