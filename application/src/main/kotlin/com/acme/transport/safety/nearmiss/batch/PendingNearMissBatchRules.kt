package com.acme.transport.safety.nearmiss.batch

import com.acme.transport.kit.text.normalize

/** A rule a pending near miss batch must pass. Check returns its problems, or none. */
interface PendingNearMissBatchRule {
    fun check(item: PendingNearMissBatch): MutableList<String>
}

data class PendingNearMissBatchRateRangeRule(var min: Double, var max: Double) : PendingNearMissBatchRule {
    override fun check(item: PendingNearMissBatch): MutableList<String> {
        val value = item.rate
        if (value < min || value > max) {
            return mutableListOf<String>("rate $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PendingNearMissBatchCodePrefixRule(var prefix: String) : PendingNearMissBatchRule {
    override fun check(item: PendingNearMissBatch): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class PendingNearMissBatchRequiredCodeRule(var message: String) : PendingNearMissBatchRule {
    override fun check(item: PendingNearMissBatch): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingNearMissBatchAllowedTrailerIdRule(
    var allowed: MutableList<String>,
) : PendingNearMissBatchRule {
    override fun check(item: PendingNearMissBatch): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.trailerId))) {
            return mutableListOf<String>("trailer id ${item.trailerId} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class PendingNearMissBatchTrailerIdLengthRule(var maxLength: Int) : PendingNearMissBatchRule {
    override fun check(item: PendingNearMissBatch): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending near miss batch. */
data class PendingNearMissBatchValidator(var rules: MutableList<PendingNearMissBatchRule>) {
    fun validate(item: PendingNearMissBatch): MutableList<String> {
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

    fun requireValid(item: PendingNearMissBatch) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingNearMissBatch>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingNearMissBatchValidator(): PendingNearMissBatchValidator {
    return PendingNearMissBatchValidator(
        mutableListOf<PendingNearMissBatchRule>(
            PendingNearMissBatchRateRangeRule(6.9, 2478.6),
            PendingNearMissBatchCodePrefixRule("OPS"),
            PendingNearMissBatchRequiredCodeRule("pending near miss batch needs a value"),
            PendingNearMissBatchAllowedTrailerIdRule(mutableListOf<String>("weighted", "batched")),
            PendingNearMissBatchTrailerIdLengthRule(16),
        ),
    )
}
