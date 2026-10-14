package com.acme.transport.network.servicearea.manifest

/** A rule a service area manifest must pass. Check returns its problems, or none. */
interface ServiceAreaManifestRule {
    fun check(item: ServiceAreaManifest): MutableList<String>
}

data class ServiceAreaManifestRequiredUnitRule(var message: String) : ServiceAreaManifestRule {
    override fun check(item: ServiceAreaManifest): MutableList<String> {
        if (item.unit.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ServiceAreaManifestDistanceKmRangeRule(var min: Double, var max: Double) : ServiceAreaManifestRule {
    override fun check(item: ServiceAreaManifest): MutableList<String> {
        val value = item.distanceKm
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("distance km $value is below $min")
        }
        if (value > max) {
            problems.add("distance km $value is above $max")
        }
        return problems
    }
}

data class ServiceAreaManifestCodeLengthRule(var maxLength: Int) : ServiceAreaManifestRule {
    override fun check(item: ServiceAreaManifest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ServiceAreaManifestUnitLengthRule(var maxLength: Int) : ServiceAreaManifestRule {
    override fun check(item: ServiceAreaManifest): MutableList<String> {
        val size = item.unit.length
        if (size > maxLength) {
            return mutableListOf<String>("unit is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a service area manifest. */
data class ServiceAreaManifestValidator(var rules: MutableList<ServiceAreaManifestRule>) {
    fun validate(item: ServiceAreaManifest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ServiceAreaManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ServiceAreaManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultServiceAreaManifestValidator(): ServiceAreaManifestValidator {
    return ServiceAreaManifestValidator(
        mutableListOf<ServiceAreaManifestRule>(
            ServiceAreaManifestRequiredUnitRule("service area manifest needs a value"),
            ServiceAreaManifestDistanceKmRangeRule(3.9, 1869.1),
            ServiceAreaManifestCodeLengthRule(3600),
            ServiceAreaManifestUnitLengthRule(120),
        ),
    )
}
