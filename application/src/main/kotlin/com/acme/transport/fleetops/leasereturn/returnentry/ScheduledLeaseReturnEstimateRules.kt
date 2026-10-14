package com.acme.transport.fleetops.leasereturn.returnentry

/** A rule a scheduled lease return estimate must pass. Check returns its problems, or none. */
interface ScheduledLeaseReturnEstimateRule {
    fun check(item: ScheduledLeaseReturnEstimate): MutableList<String>
}

data class ScheduledLeaseReturnEstimateTrailerIdLengthRule(
    var maxLength: Int,
) : ScheduledLeaseReturnEstimateRule {
    override fun check(item: ScheduledLeaseReturnEstimate): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledLeaseReturnEstimateLoadKgRangeRule(
    var min: Double,
    var max: Double,
) : ScheduledLeaseReturnEstimateRule {
    override fun check(item: ScheduledLeaseReturnEstimate): MutableList<String> {
        val value = item.loadKg
        if (value < min || value > max) {
            return mutableListOf<String>("load kg $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledLeaseReturnEstimateExpiresAtRangeRule(
    var min: Int,
    var max: Int,
) : ScheduledLeaseReturnEstimateRule {
    override fun check(item: ScheduledLeaseReturnEstimate): MutableList<String> {
        val value = item.expiresAt
        if (value < min || value > max) {
            return mutableListOf<String>("expires at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledLeaseReturnEstimateRequiredTrailerIdRule(
    var message: String,
) : ScheduledLeaseReturnEstimateRule {
    override fun check(item: ScheduledLeaseReturnEstimate): MutableList<String> {
        if (item.trailerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledLeaseReturnEstimateRatioRangeRule(
    var min: Double,
    var max: Double,
) : ScheduledLeaseReturnEstimateRule {
    override fun check(item: ScheduledLeaseReturnEstimate): MutableList<String> {
        val value = item.ratio
        if (value < min || value > max) {
            return mutableListOf<String>("ratio $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledLeaseReturnEstimateLabelLengthRule(var maxLength: Int) : ScheduledLeaseReturnEstimateRule {
    override fun check(item: ScheduledLeaseReturnEstimate): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled lease return estimate. */
data class ScheduledLeaseReturnEstimateValidator(var rules: MutableList<ScheduledLeaseReturnEstimateRule>) {
    fun validate(item: ScheduledLeaseReturnEstimate): MutableList<String> {
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

    fun requireValid(item: ScheduledLeaseReturnEstimate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledLeaseReturnEstimate>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledLeaseReturnEstimateValidator(): ScheduledLeaseReturnEstimateValidator {
    return ScheduledLeaseReturnEstimateValidator(
        mutableListOf<ScheduledLeaseReturnEstimateRule>(
            ScheduledLeaseReturnEstimateTrailerIdLengthRule(3600),
            ScheduledLeaseReturnEstimateLoadKgRangeRule(4.7, 2603.5),
            ScheduledLeaseReturnEstimateExpiresAtRangeRule(255, 3600),
            ScheduledLeaseReturnEstimateRequiredTrailerIdRule("scheduled lease return estimate needs a value"),
            ScheduledLeaseReturnEstimateRatioRangeRule(5.9, 3186.1),
            ScheduledLeaseReturnEstimateLabelLengthRule(1000),
        ),
    )
}
