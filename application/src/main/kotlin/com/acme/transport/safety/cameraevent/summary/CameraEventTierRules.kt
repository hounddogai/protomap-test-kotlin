package com.acme.transport.safety.cameraevent.summary

import com.acme.transport.kit.text.normalize

/** A rule a camera event tier must pass. Check returns its problems, or none. */
interface CameraEventTierRule {
    fun check(item: CameraEventTier): MutableList<String>
}

data class CameraEventTierRequiredTitleRule(var message: String) : CameraEventTierRule {
    override fun check(item: CameraEventTier): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CameraEventTierCodeLengthRule(var maxLength: Int) : CameraEventTierRule {
    override fun check(item: CameraEventTier): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CameraEventTierAllowedDepotRule(var allowed: MutableList<String>) : CameraEventTierRule {
    override fun check(item: CameraEventTier): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.depot))) {
            return mutableListOf<String>("depot ${item.depot} is stale")
        }
        return mutableListOf<String>()
    }
}

data class CameraEventTierDepotLengthRule(var maxLength: Int) : CameraEventTierRule {
    override fun check(item: CameraEventTier): MutableList<String> {
        val size = item.depot.length
        if (size > maxLength) {
            return mutableListOf<String>("depot is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CameraEventTierStopCountRangeRule(var min: Int, var max: Int) : CameraEventTierRule {
    override fun check(item: CameraEventTier): MutableList<String> {
        val value = item.stopCount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("stop count $value is below $min")
        }
        if (value > max) {
            problems.add("stop count $value is above $max")
        }
        return problems
    }
}

data class CameraEventTierMaximumRangeRule(var min: Double, var max: Double) : CameraEventTierRule {
    override fun check(item: CameraEventTier): MutableList<String> {
        val value = item.maximum
        if (value < min || value > max) {
            return mutableListOf<String>("maximum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a camera event tier. */
data class CameraEventTierValidator(var rules: MutableList<CameraEventTierRule>) {
    fun validate(item: CameraEventTier): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CameraEventTier) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CameraEventTier>): MutableMap<String, MutableList<String>> {
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

fun defaultCameraEventTierValidator(): CameraEventTierValidator {
    return CameraEventTierValidator(
        mutableListOf<CameraEventTierRule>(
            CameraEventTierRequiredTitleRule("camera event tier needs a value"),
            CameraEventTierCodeLengthRule(1000),
            CameraEventTierAllowedDepotRule(mutableListOf<String>()),
            CameraEventTierDepotLengthRule(255),
            CameraEventTierStopCountRangeRule(16, 255),
            CameraEventTierMaximumRangeRule(9.1, 1132.6),
        ),
    )
}
