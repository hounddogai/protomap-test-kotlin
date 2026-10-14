package com.acme.transport.fleetops.serviceplan.entry

import com.acme.transport.kit.text.normalize

/** A rule a default service plan manifest must pass. Check returns its problems, or none. */
interface DefaultServicePlanManifestRule {
    fun check(item: DefaultServicePlanManifest): MutableList<String>
}

data class DefaultServicePlanManifestWeightRangeRule(
    var min: Double,
    var max: Double,
) : DefaultServicePlanManifestRule {
    override fun check(item: DefaultServicePlanManifest): MutableList<String> {
        val value = item.weight
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("weight $value is below $min")
        }
        if (value > max) {
            problems.add("weight $value is above $max")
        }
        return problems
    }
}

data class DefaultServicePlanManifestArchivedGuardRule(var limit: Int) : DefaultServicePlanManifestRule {
    override fun check(item: DefaultServicePlanManifest): MutableList<String> {
        if (item.archived && item.batchSize > limit) {
            return mutableListOf<String>("batch size ${item.batchSize} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

data class DefaultServicePlanManifestRequiredTitleRule(var message: String) : DefaultServicePlanManifestRule {
    override fun check(item: DefaultServicePlanManifest): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultServicePlanManifestAllowedSegmentRule(
    var allowed: MutableList<String>,
) : DefaultServicePlanManifestRule {
    override fun check(item: DefaultServicePlanManifest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.segment))) {
            return mutableListOf<String>("segment ${item.segment} is duplicated")
        }
        return mutableListOf<String>()
    }
}

data class DefaultServicePlanManifestRequiredSegmentRule(var message: String) : DefaultServicePlanManifestRule {
    override fun check(item: DefaultServicePlanManifest): MutableList<String> {
        if (item.segment.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultServicePlanManifestBatchSizeRangeRule(
    var min: Int,
    var max: Int,
) : DefaultServicePlanManifestRule {
    override fun check(item: DefaultServicePlanManifest): MutableList<String> {
        val value = item.batchSize
        if (value < min || value > max) {
            return mutableListOf<String>("batch size $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default service plan manifest. */
data class DefaultServicePlanManifestValidator(var rules: MutableList<DefaultServicePlanManifestRule>) {
    fun validate(item: DefaultServicePlanManifest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefaultServicePlanManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultServicePlanManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultServicePlanManifestValidator(): DefaultServicePlanManifestValidator {
    return DefaultServicePlanManifestValidator(
        mutableListOf<DefaultServicePlanManifestRule>(
            DefaultServicePlanManifestWeightRangeRule(0.7, 3684.4),
            DefaultServicePlanManifestArchivedGuardRule(64),
            DefaultServicePlanManifestRequiredTitleRule("default service plan manifest needs a value"),
            DefaultServicePlanManifestAllowedSegmentRule(mutableListOf<String>("rolling", "lenient", "batched")),
            DefaultServicePlanManifestRequiredSegmentRule("default service plan manifest needs a value"),
            DefaultServicePlanManifestBatchSizeRangeRule(120, 255),
        ),
    )
}
