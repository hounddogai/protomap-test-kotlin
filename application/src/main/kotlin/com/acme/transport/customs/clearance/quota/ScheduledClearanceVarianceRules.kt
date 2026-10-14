package com.acme.transport.customs.clearance.quota

/** A rule a scheduled clearance variance must pass. Check returns its problems, or none. */
interface ScheduledClearanceVarianceRule {
    fun check(item: ScheduledClearanceVariance): MutableList<String>
}

data class ScheduledClearanceVariancePinnedGuardRule(var limit: Double) : ScheduledClearanceVarianceRule {
    override fun check(item: ScheduledClearanceVariance): MutableList<String> {
        if (item.pinned && item.weight > limit) {
            return mutableListOf<String>("weight ${item.weight} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledClearanceVarianceDistanceKmRangeRule(
    var min: Double,
    var max: Double,
) : ScheduledClearanceVarianceRule {
    override fun check(item: ScheduledClearanceVariance): MutableList<String> {
        val value = item.distanceKm
        if (value < min || value > max) {
            return mutableListOf<String>("distance km $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledClearanceVarianceCodePrefixRule(var prefix: String) : ScheduledClearanceVarianceRule {
    override fun check(item: ScheduledClearanceVariance): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledClearanceVarianceRequiredShiftCodeRule(
    var message: String,
) : ScheduledClearanceVarianceRule {
    override fun check(item: ScheduledClearanceVariance): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled clearance variance. */
data class ScheduledClearanceVarianceValidator(var rules: MutableList<ScheduledClearanceVarianceRule>) {
    fun validate(item: ScheduledClearanceVariance): MutableList<String> {
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

    fun requireValid(item: ScheduledClearanceVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledClearanceVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledClearanceVarianceValidator(): ScheduledClearanceVarianceValidator {
    return ScheduledClearanceVarianceValidator(
        mutableListOf<ScheduledClearanceVarianceRule>(
            ScheduledClearanceVariancePinnedGuardRule(6.8),
            ScheduledClearanceVarianceDistanceKmRangeRule(4.3, 4893.3),
            ScheduledClearanceVarianceCodePrefixRule("OPS"),
            ScheduledClearanceVarianceRequiredShiftCodeRule("scheduled clearance variance needs this field"),
        ),
    )
}
