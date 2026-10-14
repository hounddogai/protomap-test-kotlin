package com.acme.transport.linehaul.laneplan.plancheckpoint

import com.acme.transport.kit.text.normalize

/** A rule a fallback lane plan checkpoint must pass. Check returns its problems, or none. */
interface FallbackLanePlanCheckpointRule {
    fun check(item: FallbackLanePlanCheckpoint): MutableList<String>
}

data class FallbackLanePlanCheckpointDockLengthRule(var maxLength: Int) : FallbackLanePlanCheckpointRule {
    override fun check(item: FallbackLanePlanCheckpoint): MutableList<String> {
        val size = item.dock.length
        if (size > maxLength) {
            return mutableListOf<String>("dock is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackLanePlanCheckpointCurrencyLengthRule(var maxLength: Int) : FallbackLanePlanCheckpointRule {
    override fun check(item: FallbackLanePlanCheckpoint): MutableList<String> {
        val size = item.currency.length
        if (size > maxLength) {
            return mutableListOf<String>("currency is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackLanePlanCheckpointPalletsRangeRule(
    var min: Int,
    var max: Int,
) : FallbackLanePlanCheckpointRule {
    override fun check(item: FallbackLanePlanCheckpoint): MutableList<String> {
        val value = item.pallets
        if (value < min || value > max) {
            return mutableListOf<String>("pallets $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FallbackLanePlanCheckpointAllowedCodeRule(
    var allowed: MutableList<String>,
) : FallbackLanePlanCheckpointRule {
    override fun check(item: FallbackLanePlanCheckpoint): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class FallbackLanePlanCheckpointTitleLengthRule(var maxLength: Int) : FallbackLanePlanCheckpointRule {
    override fun check(item: FallbackLanePlanCheckpoint): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback lane plan checkpoint. */
data class FallbackLanePlanCheckpointValidator(var rules: MutableList<FallbackLanePlanCheckpointRule>) {
    fun validate(item: FallbackLanePlanCheckpoint): MutableList<String> {
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

    fun requireValid(item: FallbackLanePlanCheckpoint) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackLanePlanCheckpoint>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackLanePlanCheckpointValidator(): FallbackLanePlanCheckpointValidator {
    return FallbackLanePlanCheckpointValidator(
        mutableListOf<FallbackLanePlanCheckpointRule>(
            FallbackLanePlanCheckpointDockLengthRule(120),
            FallbackLanePlanCheckpointCurrencyLengthRule(3600),
            FallbackLanePlanCheckpointPalletsRangeRule(120, 120),
            FallbackLanePlanCheckpointAllowedCodeRule(mutableListOf<String>("primary", "global")),
            FallbackLanePlanCheckpointTitleLengthRule(32),
        ),
    )
}
