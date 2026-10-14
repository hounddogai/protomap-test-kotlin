package com.acme.transport.fleetops.serviceplan.checkpoint

/** A rule a service plan checkpoint must pass. Check returns its problems, or none. */
interface ServicePlanCheckpointRule {
    fun check(item: ServicePlanCheckpoint): MutableList<String>
}

data class ServicePlanCheckpointRequiredNameRule(var message: String) : ServicePlanCheckpointRule {
    override fun check(item: ServicePlanCheckpoint): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanCheckpointRequiredExternalRefRule(var message: String) : ServicePlanCheckpointRule {
    override fun check(item: ServicePlanCheckpoint): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanCheckpointLoadKgRangeRule(var min: Double, var max: Double) : ServicePlanCheckpointRule {
    override fun check(item: ServicePlanCheckpoint): MutableList<String> {
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

data class ServicePlanCheckpointNameLengthRule(var maxLength: Int) : ServicePlanCheckpointRule {
    override fun check(item: ServicePlanCheckpoint): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanCheckpointDriverIdLengthRule(var maxLength: Int) : ServicePlanCheckpointRule {
    override fun check(item: ServicePlanCheckpoint): MutableList<String> {
        val size = item.driverId.length
        if (size > maxLength) {
            return mutableListOf<String>("driver id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanCheckpointScoreRangeRule(var min: Double, var max: Double) : ServicePlanCheckpointRule {
    override fun check(item: ServicePlanCheckpoint): MutableList<String> {
        val value = item.score
        if (value < min || value > max) {
            return mutableListOf<String>("score $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a service plan checkpoint. */
data class ServicePlanCheckpointValidator(var rules: MutableList<ServicePlanCheckpointRule>) {
    fun validate(item: ServicePlanCheckpoint): MutableList<String> {
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

    fun requireValid(item: ServicePlanCheckpoint) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ServicePlanCheckpoint>): MutableMap<String, MutableList<String>> {
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

fun defaultServicePlanCheckpointValidator(): ServicePlanCheckpointValidator {
    return ServicePlanCheckpointValidator(
        mutableListOf<ServicePlanCheckpointRule>(
            ServicePlanCheckpointRequiredNameRule("service plan checkpoint needs an entry"),
            ServicePlanCheckpointRequiredExternalRefRule("service plan checkpoint needs an entry"),
            ServicePlanCheckpointLoadKgRangeRule(1.1, 4959.1),
            ServicePlanCheckpointNameLengthRule(1000),
            ServicePlanCheckpointDriverIdLengthRule(1000),
            ServicePlanCheckpointScoreRangeRule(5.7, 4623.9),
        ),
    )
}
