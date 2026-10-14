package com.acme.transport.safety.cameraevent.audit

import com.acme.transport.kit.text.normalize

/** A rule a pending camera event forecast must pass. Check returns its problems, or none. */
interface PendingCameraEventForecastRule {
    fun check(item: PendingCameraEventForecast): MutableList<String>
}

data class PendingCameraEventForecastAllowedCodeRule(
    var allowed: MutableList<String>,
) : PendingCameraEventForecastRule {
    override fun check(item: PendingCameraEventForecast): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class PendingCameraEventForecastUnitLengthRule(var maxLength: Int) : PendingCameraEventForecastRule {
    override fun check(item: PendingCameraEventForecast): MutableList<String> {
        val size = item.unit.length
        if (size > maxLength) {
            return mutableListOf<String>("unit is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingCameraEventForecastLoadKgRangeRule(
    var min: Double,
    var max: Double,
) : PendingCameraEventForecastRule {
    override fun check(item: PendingCameraEventForecast): MutableList<String> {
        val value = item.loadKg
        if (value < min || value > max) {
            return mutableListOf<String>("load kg $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PendingCameraEventForecastRequiredUnitRule(var message: String) : PendingCameraEventForecastRule {
    override fun check(item: PendingCameraEventForecast): MutableList<String> {
        if (item.unit.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending camera event forecast. */
data class PendingCameraEventForecastValidator(var rules: MutableList<PendingCameraEventForecastRule>) {
    fun validate(item: PendingCameraEventForecast): MutableList<String> {
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

    fun requireValid(item: PendingCameraEventForecast) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingCameraEventForecast>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingCameraEventForecastValidator(): PendingCameraEventForecastValidator {
    return PendingCameraEventForecastValidator(
        mutableListOf<PendingCameraEventForecastRule>(
            PendingCameraEventForecastAllowedCodeRule(mutableListOf<String>()),
            PendingCameraEventForecastUnitLengthRule(16),
            PendingCameraEventForecastLoadKgRangeRule(0.6, 2588.5),
            PendingCameraEventForecastRequiredUnitRule("pending camera event forecast needs this field"),
        ),
    )
}
