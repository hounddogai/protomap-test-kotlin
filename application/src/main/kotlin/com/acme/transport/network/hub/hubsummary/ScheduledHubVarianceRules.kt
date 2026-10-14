package com.acme.transport.network.hub.hubsummary

/** A rule a scheduled hub variance must pass. Check returns its problems, or none. */
interface ScheduledHubVarianceRule {
    fun check(item: ScheduledHubVariance): MutableList<String>
}

data class ScheduledHubVarianceCodePrefixRule(var prefix: String) : ScheduledHubVarianceRule {
    override fun check(item: ScheduledHubVariance): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledHubVarianceDistanceKmRangeRule(
    var min: Double,
    var max: Double,
) : ScheduledHubVarianceRule {
    override fun check(item: ScheduledHubVariance): MutableList<String> {
        val value = item.distanceKm
        if (value < min || value > max) {
            return mutableListOf<String>("distance km $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledHubVarianceRouteCodeLengthRule(var maxLength: Int) : ScheduledHubVarianceRule {
    override fun check(item: ScheduledHubVariance): MutableList<String> {
        val size = item.routeCode.length
        if (size > maxLength) {
            return mutableListOf<String>("route code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledHubVarianceCodeLengthRule(var maxLength: Int) : ScheduledHubVarianceRule {
    override fun check(item: ScheduledHubVariance): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledHubVarianceRequiredLabelRule(var message: String) : ScheduledHubVarianceRule {
    override fun check(item: ScheduledHubVariance): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled hub variance. */
data class ScheduledHubVarianceValidator(var rules: MutableList<ScheduledHubVarianceRule>) {
    fun validate(item: ScheduledHubVariance): MutableList<String> {
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

    fun requireValid(item: ScheduledHubVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledHubVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledHubVarianceValidator(): ScheduledHubVarianceValidator {
    return ScheduledHubVarianceValidator(
        mutableListOf<ScheduledHubVarianceRule>(
            ScheduledHubVarianceCodePrefixRule("REF"),
            ScheduledHubVarianceDistanceKmRangeRule(3.4, 582.8),
            ScheduledHubVarianceRouteCodeLengthRule(16),
            ScheduledHubVarianceCodeLengthRule(16),
            ScheduledHubVarianceRequiredLabelRule("scheduled hub variance needs an entry"),
        ),
    )
}
