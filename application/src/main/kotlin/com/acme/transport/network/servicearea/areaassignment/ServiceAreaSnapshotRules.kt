package com.acme.transport.network.servicearea.areaassignment

import com.acme.transport.kit.text.normalize

/** A rule a service area snapshot must pass. Check returns its problems, or none. */
interface ServiceAreaSnapshotRule {
    fun check(item: ServiceAreaSnapshot): MutableList<String>
}

data class ServiceAreaSnapshotAllowedRegionRule(var allowed: MutableList<String>) : ServiceAreaSnapshotRule {
    override fun check(item: ServiceAreaSnapshot): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.region))) {
            return mutableListOf<String>("region ${item.region} is expired")
        }
        return mutableListOf<String>()
    }
}

data class ServiceAreaSnapshotCodeLengthRule(var maxLength: Int) : ServiceAreaSnapshotRule {
    override fun check(item: ServiceAreaSnapshot): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ServiceAreaSnapshotCodePrefixRule(var prefix: String) : ServiceAreaSnapshotRule {
    override fun check(item: ServiceAreaSnapshot): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ServiceAreaSnapshotVersionRangeRule(var min: Int, var max: Int) : ServiceAreaSnapshotRule {
    override fun check(item: ServiceAreaSnapshot): MutableList<String> {
        val value = item.version
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("version $value is below $min")
        }
        if (value > max) {
            problems.add("version $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a service area snapshot. */
data class ServiceAreaSnapshotValidator(var rules: MutableList<ServiceAreaSnapshotRule>) {
    fun validate(item: ServiceAreaSnapshot): MutableList<String> {
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

    fun requireValid(item: ServiceAreaSnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ServiceAreaSnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultServiceAreaSnapshotValidator(): ServiceAreaSnapshotValidator {
    return ServiceAreaSnapshotValidator(
        mutableListOf<ServiceAreaSnapshotRule>(
            ServiceAreaSnapshotAllowedRegionRule(mutableListOf<String>("legacy", "global")),
            ServiceAreaSnapshotCodeLengthRule(16),
            ServiceAreaSnapshotCodePrefixRule("REF"),
            ServiceAreaSnapshotVersionRangeRule(16, 64),
        ),
    )
}
