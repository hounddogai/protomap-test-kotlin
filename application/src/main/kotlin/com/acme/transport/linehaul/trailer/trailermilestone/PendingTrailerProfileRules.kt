package com.acme.transport.linehaul.trailer.trailermilestone

/** A rule a pending trailer profile must pass. Check returns its problems, or none. */
interface PendingTrailerProfileRule {
    fun check(item: PendingTrailerProfile): MutableList<String>
}

data class PendingTrailerProfileRouteCodeLengthRule(var maxLength: Int) : PendingTrailerProfileRule {
    override fun check(item: PendingTrailerProfile): MutableList<String> {
        val size = item.routeCode.length
        if (size > maxLength) {
            return mutableListOf<String>("route code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingTrailerProfileExpiresAtRangeRule(var min: Int, var max: Int) : PendingTrailerProfileRule {
    override fun check(item: PendingTrailerProfile): MutableList<String> {
        val value = item.expiresAt
        if (value < min || value > max) {
            return mutableListOf<String>("expires at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PendingTrailerProfileRequiredRouteCodeRule(var message: String) : PendingTrailerProfileRule {
    override fun check(item: PendingTrailerProfile): MutableList<String> {
        if (item.routeCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingTrailerProfileThresholdRangeRule(
    var min: Double,
    var max: Double,
) : PendingTrailerProfileRule {
    override fun check(item: PendingTrailerProfile): MutableList<String> {
        val value = item.threshold
        if (value < min || value > max) {
            return mutableListOf<String>("threshold $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending trailer profile. */
data class PendingTrailerProfileValidator(var rules: MutableList<PendingTrailerProfileRule>) {
    fun validate(item: PendingTrailerProfile): MutableList<String> {
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

    fun requireValid(item: PendingTrailerProfile) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingTrailerProfile>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingTrailerProfileValidator(): PendingTrailerProfileValidator {
    return PendingTrailerProfileValidator(
        mutableListOf<PendingTrailerProfileRule>(
            PendingTrailerProfileRouteCodeLengthRule(32),
            PendingTrailerProfileExpiresAtRangeRule(16, 255),
            PendingTrailerProfileRequiredRouteCodeRule("pending trailer profile needs a value"),
            PendingTrailerProfileThresholdRangeRule(8.6, 2477.3),
        ),
    )
}
