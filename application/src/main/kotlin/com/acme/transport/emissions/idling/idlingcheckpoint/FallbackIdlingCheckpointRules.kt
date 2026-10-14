package com.acme.transport.emissions.idling.idlingcheckpoint

import com.acme.transport.kit.text.normalize

/** A rule a fallback idling checkpoint must pass. Check returns its problems, or none. */
interface FallbackIdlingCheckpointRule {
    fun check(item: FallbackIdlingCheckpoint): MutableList<String>
}

data class FallbackIdlingCheckpointRateRangeRule(
    var min: Double,
    var max: Double,
) : FallbackIdlingCheckpointRule {
    override fun check(item: FallbackIdlingCheckpoint): MutableList<String> {
        val value = item.rate
        if (value < min || value > max) {
            return mutableListOf<String>("rate $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FallbackIdlingCheckpointRequiredNameRule(var message: String) : FallbackIdlingCheckpointRule {
    override fun check(item: FallbackIdlingCheckpoint): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackIdlingCheckpointRequiredDescriptionRule(var message: String) : FallbackIdlingCheckpointRule {
    override fun check(item: FallbackIdlingCheckpoint): MutableList<String> {
        if (item.description.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackIdlingCheckpointCodePrefixRule(var prefix: String) : FallbackIdlingCheckpointRule {
    override fun check(item: FallbackIdlingCheckpoint): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class FallbackIdlingCheckpointCodeLengthRule(var maxLength: Int) : FallbackIdlingCheckpointRule {
    override fun check(item: FallbackIdlingCheckpoint): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackIdlingCheckpointAllowedNotesRule(
    var allowed: MutableList<String>,
) : FallbackIdlingCheckpointRule {
    override fun check(item: FallbackIdlingCheckpoint): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.notes))) {
            return mutableListOf<String>("notes ${item.notes} is too long")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback idling checkpoint. */
data class FallbackIdlingCheckpointValidator(var rules: MutableList<FallbackIdlingCheckpointRule>) {
    fun validate(item: FallbackIdlingCheckpoint): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FallbackIdlingCheckpoint) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackIdlingCheckpoint>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackIdlingCheckpointValidator(): FallbackIdlingCheckpointValidator {
    return FallbackIdlingCheckpointValidator(
        mutableListOf<FallbackIdlingCheckpointRule>(
            FallbackIdlingCheckpointRateRangeRule(6.6, 3116.7),
            FallbackIdlingCheckpointRequiredNameRule("fallback idling checkpoint needs this field"),
            FallbackIdlingCheckpointRequiredDescriptionRule("fallback idling checkpoint needs a value"),
            FallbackIdlingCheckpointCodePrefixRule("OPS"),
            FallbackIdlingCheckpointCodeLengthRule(86400),
            FallbackIdlingCheckpointAllowedNotesRule(mutableListOf<String>("manual", "regional", "primary")),
        ),
    )
}
