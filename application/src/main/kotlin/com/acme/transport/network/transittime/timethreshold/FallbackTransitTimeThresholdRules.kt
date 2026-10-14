package com.acme.transport.network.transittime.timethreshold

/** A rule a fallback transit time threshold must pass. Check returns its problems, or none. */
interface FallbackTransitTimeThresholdRule {
    fun check(item: FallbackTransitTimeThreshold): MutableList<String>
}

data class FallbackTransitTimeThresholdEtaMinutesRangeRule(
    var min: Int,
    var max: Int,
) : FallbackTransitTimeThresholdRule {
    override fun check(item: FallbackTransitTimeThreshold): MutableList<String> {
        val value = item.etaMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("eta minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FallbackTransitTimeThresholdRequiredSourceRule(
    var message: String,
) : FallbackTransitTimeThresholdRule {
    override fun check(item: FallbackTransitTimeThreshold): MutableList<String> {
        if (item.source.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackTransitTimeThresholdCodeLengthRule(var maxLength: Int) : FallbackTransitTimeThresholdRule {
    override fun check(item: FallbackTransitTimeThreshold): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackTransitTimeThresholdRequiredCodeRule(
    var message: String,
) : FallbackTransitTimeThresholdRule {
    override fun check(item: FallbackTransitTimeThreshold): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackTransitTimeThresholdRequiredNameRule(
    var message: String,
) : FallbackTransitTimeThresholdRule {
    override fun check(item: FallbackTransitTimeThreshold): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback transit time threshold. */
data class FallbackTransitTimeThresholdValidator(var rules: MutableList<FallbackTransitTimeThresholdRule>) {
    fun validate(item: FallbackTransitTimeThreshold): MutableList<String> {
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

    fun requireValid(item: FallbackTransitTimeThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackTransitTimeThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackTransitTimeThresholdValidator(): FallbackTransitTimeThresholdValidator {
    return FallbackTransitTimeThresholdValidator(
        mutableListOf<FallbackTransitTimeThresholdRule>(
            FallbackTransitTimeThresholdEtaMinutesRangeRule(64, 64),
            FallbackTransitTimeThresholdRequiredSourceRule("fallback transit time threshold needs a value"),
            FallbackTransitTimeThresholdCodeLengthRule(3600),
            FallbackTransitTimeThresholdRequiredCodeRule("fallback transit time threshold needs this field"),
            FallbackTransitTimeThresholdRequiredNameRule("fallback transit time threshold needs this field"),
        ),
    )
}
