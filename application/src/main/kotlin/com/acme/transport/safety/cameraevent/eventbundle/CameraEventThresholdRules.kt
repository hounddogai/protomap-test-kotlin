package com.acme.transport.safety.cameraevent.eventbundle

import com.acme.transport.kit.text.normalize

/** A rule a camera event threshold must pass. Check returns its problems, or none. */
interface CameraEventThresholdRule {
    fun check(item: CameraEventThreshold): MutableList<String>
}

data class CameraEventThresholdCodeLengthRule(var maxLength: Int) : CameraEventThresholdRule {
    override fun check(item: CameraEventThreshold): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CameraEventThresholdCodePrefixRule(var prefix: String) : CameraEventThresholdRule {
    override fun check(item: CameraEventThreshold): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class CameraEventThresholdCountryLengthRule(var maxLength: Int) : CameraEventThresholdRule {
    override fun check(item: CameraEventThreshold): MutableList<String> {
        val size = item.country.length
        if (size > maxLength) {
            return mutableListOf<String>("country is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CameraEventThresholdAllowedCountryRule(var allowed: MutableList<String>) : CameraEventThresholdRule {
    override fun check(item: CameraEventThreshold): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.country))) {
            return mutableListOf<String>("country ${item.country} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class CameraEventThresholdLimitRangeRule(var min: Int, var max: Int) : CameraEventThresholdRule {
    override fun check(item: CameraEventThreshold): MutableList<String> {
        val value = item.limit
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("limit $value is below $min")
        }
        if (value > max) {
            problems.add("limit $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a camera event threshold. */
data class CameraEventThresholdValidator(var rules: MutableList<CameraEventThresholdRule>) {
    fun validate(item: CameraEventThreshold): MutableList<String> {
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

    fun requireValid(item: CameraEventThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CameraEventThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultCameraEventThresholdValidator(): CameraEventThresholdValidator {
    return CameraEventThresholdValidator(
        mutableListOf<CameraEventThresholdRule>(
            CameraEventThresholdCodeLengthRule(16),
            CameraEventThresholdCodePrefixRule("OPS"),
            CameraEventThresholdCountryLengthRule(64),
            CameraEventThresholdAllowedCountryRule(mutableListOf<String>("default")),
            CameraEventThresholdLimitRangeRule(32, 86400),
        ),
    )
}
