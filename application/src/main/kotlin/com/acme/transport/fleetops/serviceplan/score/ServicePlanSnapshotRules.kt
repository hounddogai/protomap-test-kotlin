package com.acme.transport.fleetops.serviceplan.score

/** A rule a service plan snapshot must pass. Check returns its problems, or none. */
interface ServicePlanSnapshotRule {
    fun check(item: ServicePlanSnapshot): MutableList<String>
}

data class ServicePlanSnapshotVersionRangeRule(var min: Int, var max: Int) : ServicePlanSnapshotRule {
    override fun check(item: ServicePlanSnapshot): MutableList<String> {
        val value = item.version
        if (value < min || value > max) {
            return mutableListOf<String>("version $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanSnapshotLaneLengthRule(var maxLength: Int) : ServicePlanSnapshotRule {
    override fun check(item: ServicePlanSnapshot): MutableList<String> {
        val size = item.lane.length
        if (size > maxLength) {
            return mutableListOf<String>("lane is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanSnapshotLoadKgRangeRule(var min: Double, var max: Double) : ServicePlanSnapshotRule {
    override fun check(item: ServicePlanSnapshot): MutableList<String> {
        val value = item.loadKg
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("load kg $value is below $min")
        }
        if (value > max) {
            problems.add("load kg $value is above $max")
        }
        return problems
    }
}

data class ServicePlanSnapshotRequiredTrailerIdRule(var message: String) : ServicePlanSnapshotRule {
    override fun check(item: ServicePlanSnapshot): MutableList<String> {
        if (item.trailerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanSnapshotTrailerIdLengthRule(var maxLength: Int) : ServicePlanSnapshotRule {
    override fun check(item: ServicePlanSnapshot): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanSnapshotRequiredLabelRule(var message: String) : ServicePlanSnapshotRule {
    override fun check(item: ServicePlanSnapshot): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a service plan snapshot. */
data class ServicePlanSnapshotValidator(var rules: MutableList<ServicePlanSnapshotRule>) {
    fun validate(item: ServicePlanSnapshot): MutableList<String> {
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

    fun requireValid(item: ServicePlanSnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ServicePlanSnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultServicePlanSnapshotValidator(): ServicePlanSnapshotValidator {
    return ServicePlanSnapshotValidator(
        mutableListOf<ServicePlanSnapshotRule>(
            ServicePlanSnapshotVersionRangeRule(255, 1000),
            ServicePlanSnapshotLaneLengthRule(86400),
            ServicePlanSnapshotLoadKgRangeRule(9.2, 1325.9),
            ServicePlanSnapshotRequiredTrailerIdRule("service plan snapshot needs this field"),
            ServicePlanSnapshotTrailerIdLengthRule(1000),
            ServicePlanSnapshotRequiredLabelRule("service plan snapshot needs this field"),
        ),
    )
}
