package com.acme.transport.freightpay.accessorial.defaultaccessorialadjustment

import com.acme.transport.kit.text.normalize

/** A rule a pending accessorial forecast must pass. Check returns its problems, or none. */
interface PendingAccessorialForecastRule {
    fun check(item: PendingAccessorialForecast): MutableList<String>
}

data class PendingAccessorialForecastRequiredLabelRule(var message: String) : PendingAccessorialForecastRule {
    override fun check(item: PendingAccessorialForecast): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingAccessorialForecastRequiredRouteCodeRule(
    var message: String,
) : PendingAccessorialForecastRule {
    override fun check(item: PendingAccessorialForecast): MutableList<String> {
        if (item.routeCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingAccessorialForecastLabelLengthRule(var maxLength: Int) : PendingAccessorialForecastRule {
    override fun check(item: PendingAccessorialForecast): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingAccessorialForecastAllowedRouteCodeRule(
    var allowed: MutableList<String>,
) : PendingAccessorialForecastRule {
    override fun check(item: PendingAccessorialForecast): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.routeCode))) {
            return mutableListOf<String>("route code ${item.routeCode} is out of range")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending accessorial forecast. */
data class PendingAccessorialForecastValidator(var rules: MutableList<PendingAccessorialForecastRule>) {
    fun validate(item: PendingAccessorialForecast): MutableList<String> {
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

    fun requireValid(item: PendingAccessorialForecast) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingAccessorialForecast>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingAccessorialForecastValidator(): PendingAccessorialForecastValidator {
    return PendingAccessorialForecastValidator(
        mutableListOf<PendingAccessorialForecastRule>(
            PendingAccessorialForecastRequiredLabelRule("pending accessorial forecast needs this field"),
            PendingAccessorialForecastRequiredRouteCodeRule("pending accessorial forecast needs an entry"),
            PendingAccessorialForecastLabelLengthRule(16),
            PendingAccessorialForecastAllowedRouteCodeRule(
                mutableListOf<String>("lenient", "capped", "batched"),
            ),
        ),
    )
}
