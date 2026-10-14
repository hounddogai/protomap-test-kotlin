package com.acme.transport.freightpay.dispute.adjustment

/** A rule a scheduled dispute adjustment must pass. Check returns its problems, or none. */
interface ScheduledDisputeAdjustmentRule {
    fun check(item: ScheduledDisputeAdjustment): MutableList<String>
}

data class ScheduledDisputeAdjustmentRequiredLabelRule(var message: String) : ScheduledDisputeAdjustmentRule {
    override fun check(item: ScheduledDisputeAdjustment): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledDisputeAdjustmentEndsAtRangeRule(
    var min: Int,
    var max: Int,
) : ScheduledDisputeAdjustmentRule {
    override fun check(item: ScheduledDisputeAdjustment): MutableList<String> {
        val value = item.endsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ends at $value is below $min")
        }
        if (value > max) {
            problems.add("ends at $value is above $max")
        }
        return problems
    }
}

data class ScheduledDisputeAdjustmentVehicleIdLengthRule(var maxLength: Int) : ScheduledDisputeAdjustmentRule {
    override fun check(item: ScheduledDisputeAdjustment): MutableList<String> {
        val size = item.vehicleId.length
        if (size > maxLength) {
            return mutableListOf<String>("vehicle id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledDisputeAdjustmentUpdatedAtRangeRule(
    var min: Int,
    var max: Int,
) : ScheduledDisputeAdjustmentRule {
    override fun check(item: ScheduledDisputeAdjustment): MutableList<String> {
        val value = item.updatedAt
        if (value < min || value > max) {
            return mutableListOf<String>("updated at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledDisputeAdjustmentRetriesRangeRule(
    var min: Int,
    var max: Int,
) : ScheduledDisputeAdjustmentRule {
    override fun check(item: ScheduledDisputeAdjustment): MutableList<String> {
        val value = item.retries
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("retries $value is below $min")
        }
        if (value > max) {
            problems.add("retries $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a scheduled dispute adjustment. */
data class ScheduledDisputeAdjustmentValidator(var rules: MutableList<ScheduledDisputeAdjustmentRule>) {
    fun validate(item: ScheduledDisputeAdjustment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ScheduledDisputeAdjustment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledDisputeAdjustment>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledDisputeAdjustmentValidator(): ScheduledDisputeAdjustmentValidator {
    return ScheduledDisputeAdjustmentValidator(
        mutableListOf<ScheduledDisputeAdjustmentRule>(
            ScheduledDisputeAdjustmentRequiredLabelRule("scheduled dispute adjustment needs an entry"),
            ScheduledDisputeAdjustmentEndsAtRangeRule(120, 1000),
            ScheduledDisputeAdjustmentVehicleIdLengthRule(3600),
            ScheduledDisputeAdjustmentUpdatedAtRangeRule(32, 3600),
            ScheduledDisputeAdjustmentRetriesRangeRule(16, 86400),
        ),
    )
}
