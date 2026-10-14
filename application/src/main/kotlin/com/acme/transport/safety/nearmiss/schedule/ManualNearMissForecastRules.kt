package com.acme.transport.safety.nearmiss.schedule

import com.acme.transport.kit.text.normalize

/** A rule a manual near miss forecast must pass. Check returns its problems, or none. */
interface ManualNearMissForecastRule {
    fun check(item: ManualNearMissForecast): MutableList<String>
}

data class ManualNearMissForecastStopCountRangeRule(var min: Int, var max: Int) : ManualNearMissForecastRule {
    override fun check(item: ManualNearMissForecast): MutableList<String> {
        val value = item.stopCount
        if (value < min || value > max) {
            return mutableListOf<String>("stop count $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ManualNearMissForecastAllowedLabelRule(
    var allowed: MutableList<String>,
) : ManualNearMissForecastRule {
    override fun check(item: ManualNearMissForecast): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is too long")
        }
        return mutableListOf<String>()
    }
}

data class ManualNearMissForecastBatchSizeRangeRule(var min: Int, var max: Int) : ManualNearMissForecastRule {
    override fun check(item: ManualNearMissForecast): MutableList<String> {
        val value = item.batchSize
        if (value < min || value > max) {
            return mutableListOf<String>("batch size $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ManualNearMissForecastCodePrefixRule(var prefix: String) : ManualNearMissForecastRule {
    override fun check(item: ManualNearMissForecast): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ManualNearMissForecastRequiredLabelRule(var message: String) : ManualNearMissForecastRule {
    override fun check(item: ManualNearMissForecast): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual near miss forecast. */
data class ManualNearMissForecastValidator(var rules: MutableList<ManualNearMissForecastRule>) {
    fun validate(item: ManualNearMissForecast): MutableList<String> {
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

    fun requireValid(item: ManualNearMissForecast) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualNearMissForecast>): MutableMap<String, MutableList<String>> {
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

fun defaultManualNearMissForecastValidator(): ManualNearMissForecastValidator {
    return ManualNearMissForecastValidator(
        mutableListOf<ManualNearMissForecastRule>(
            ManualNearMissForecastStopCountRangeRule(120, 3600),
            ManualNearMissForecastAllowedLabelRule(mutableListOf<String>("primary", "global")),
            ManualNearMissForecastBatchSizeRangeRule(80, 86400),
            ManualNearMissForecastCodePrefixRule("REF"),
            ManualNearMissForecastRequiredLabelRule("manual near miss forecast needs an entry"),
        ),
    )
}
