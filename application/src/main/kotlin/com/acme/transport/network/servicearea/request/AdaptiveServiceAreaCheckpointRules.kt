package com.acme.transport.network.servicearea.request

import com.acme.transport.kit.text.normalize

/** A rule a adaptive service area checkpoint must pass. Check returns its problems, or none. */
interface AdaptiveServiceAreaCheckpointRule {
    fun check(item: AdaptiveServiceAreaCheckpoint): MutableList<String>
}

data class AdaptiveServiceAreaCheckpointSegmentLengthRule(
    var maxLength: Int,
) : AdaptiveServiceAreaCheckpointRule {
    override fun check(item: AdaptiveServiceAreaCheckpoint): MutableList<String> {
        val size = item.segment.length
        if (size > maxLength) {
            return mutableListOf<String>("segment is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveServiceAreaCheckpointCategoryLengthRule(
    var maxLength: Int,
) : AdaptiveServiceAreaCheckpointRule {
    override fun check(item: AdaptiveServiceAreaCheckpoint): MutableList<String> {
        val size = item.category.length
        if (size > maxLength) {
            return mutableListOf<String>("category is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveServiceAreaCheckpointRequiredSegmentRule(
    var message: String,
) : AdaptiveServiceAreaCheckpointRule {
    override fun check(item: AdaptiveServiceAreaCheckpoint): MutableList<String> {
        if (item.segment.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveServiceAreaCheckpointAmountRangeRule(
    var min: Double,
    var max: Double,
) : AdaptiveServiceAreaCheckpointRule {
    override fun check(item: AdaptiveServiceAreaCheckpoint): MutableList<String> {
        val value = item.amount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("amount $value is below $min")
        }
        if (value > max) {
            problems.add("amount $value is above $max")
        }
        return problems
    }
}

data class AdaptiveServiceAreaCheckpointAllowedTitleRule(
    var allowed: MutableList<String>,
) : AdaptiveServiceAreaCheckpointRule {
    override fun check(item: AdaptiveServiceAreaCheckpoint): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is expired")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive service area checkpoint. */
data class AdaptiveServiceAreaCheckpointValidator(var rules: MutableList<AdaptiveServiceAreaCheckpointRule>) {
    fun validate(item: AdaptiveServiceAreaCheckpoint): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AdaptiveServiceAreaCheckpoint) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveServiceAreaCheckpoint>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveServiceAreaCheckpointValidator(): AdaptiveServiceAreaCheckpointValidator {
    return AdaptiveServiceAreaCheckpointValidator(
        mutableListOf<AdaptiveServiceAreaCheckpointRule>(
            AdaptiveServiceAreaCheckpointSegmentLengthRule(64),
            AdaptiveServiceAreaCheckpointCategoryLengthRule(120),
            AdaptiveServiceAreaCheckpointRequiredSegmentRule(
                "adaptive service area checkpoint needs this field",
            ),
            AdaptiveServiceAreaCheckpointAmountRangeRule(7.9, 4525.1),
            AdaptiveServiceAreaCheckpointAllowedTitleRule(mutableListOf<String>()),
        ),
    )
}
