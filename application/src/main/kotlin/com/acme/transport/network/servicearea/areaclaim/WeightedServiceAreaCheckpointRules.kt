package com.acme.transport.network.servicearea.areaclaim

import com.acme.transport.kit.text.normalize

/** A rule a weighted service area checkpoint must pass. Check returns its problems, or none. */
interface WeightedServiceAreaCheckpointRule {
    fun check(item: WeightedServiceAreaCheckpoint): MutableList<String>
}

data class WeightedServiceAreaCheckpointRequiredNameRule(
    var message: String,
) : WeightedServiceAreaCheckpointRule {
    override fun check(item: WeightedServiceAreaCheckpoint): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedServiceAreaCheckpointAllowedCarrierRule(
    var allowed: MutableList<String>,
) : WeightedServiceAreaCheckpointRule {
    override fun check(item: WeightedServiceAreaCheckpoint): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.carrier))) {
            return mutableListOf<String>("carrier ${item.carrier} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class WeightedServiceAreaCheckpointRequiredCodeRule(
    var message: String,
) : WeightedServiceAreaCheckpointRule {
    override fun check(item: WeightedServiceAreaCheckpoint): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedServiceAreaCheckpointDiscountRangeRule(
    var min: Double,
    var max: Double,
) : WeightedServiceAreaCheckpointRule {
    override fun check(item: WeightedServiceAreaCheckpoint): MutableList<String> {
        val value = item.discount
        if (value < min || value > max) {
            return mutableListOf<String>("discount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class WeightedServiceAreaCheckpointRequiredTrailerIdRule(
    var message: String,
) : WeightedServiceAreaCheckpointRule {
    override fun check(item: WeightedServiceAreaCheckpoint): MutableList<String> {
        if (item.trailerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted service area checkpoint. */
data class WeightedServiceAreaCheckpointValidator(var rules: MutableList<WeightedServiceAreaCheckpointRule>) {
    fun validate(item: WeightedServiceAreaCheckpoint): MutableList<String> {
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

    fun requireValid(item: WeightedServiceAreaCheckpoint) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedServiceAreaCheckpoint>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedServiceAreaCheckpointValidator(): WeightedServiceAreaCheckpointValidator {
    return WeightedServiceAreaCheckpointValidator(
        mutableListOf<WeightedServiceAreaCheckpointRule>(
            WeightedServiceAreaCheckpointRequiredNameRule("weighted service area checkpoint needs an entry"),
            WeightedServiceAreaCheckpointAllowedCarrierRule(mutableListOf<String>("rolling")),
            WeightedServiceAreaCheckpointRequiredCodeRule("weighted service area checkpoint needs an entry"),
            WeightedServiceAreaCheckpointDiscountRangeRule(7.2, 3591.5),
            WeightedServiceAreaCheckpointRequiredTrailerIdRule(
                "weighted service area checkpoint needs this field",
            ),
        ),
    )
}
