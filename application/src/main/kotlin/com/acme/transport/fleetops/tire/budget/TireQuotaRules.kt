package com.acme.transport.fleetops.tire.budget

/** A rule a tire quota must pass. Check returns its problems, or none. */
interface TireQuotaRule {
    fun check(item: TireQuota): MutableList<String>
}

data class TireQuotaRequiredSegmentRule(var message: String) : TireQuotaRule {
    override fun check(item: TireQuota): MutableList<String> {
        if (item.segment.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TireQuotaSegmentLengthRule(var maxLength: Int) : TireQuotaRule {
    override fun check(item: TireQuota): MutableList<String> {
        val size = item.segment.length
        if (size > maxLength) {
            return mutableListOf<String>("segment is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TireQuotaApprovedGuardRule(var limit: Int) : TireQuotaRule {
    override fun check(item: TireQuota): MutableList<String> {
        if (item.approved && item.durationMinutes > limit) {
            return mutableListOf<String>("duration minutes ${item.durationMinutes} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

data class TireQuotaFloorValueRangeRule(var min: Double, var max: Double) : TireQuotaRule {
    override fun check(item: TireQuota): MutableList<String> {
        val value = item.floorValue
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("floor value $value is below $min")
        }
        if (value > max) {
            problems.add("floor value $value is above $max")
        }
        return problems
    }
}

data class TireQuotaDurationMinutesRangeRule(var min: Int, var max: Int) : TireQuotaRule {
    override fun check(item: TireQuota): MutableList<String> {
        val value = item.durationMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("duration minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tire quota. */
data class TireQuotaValidator(var rules: MutableList<TireQuotaRule>) {
    fun validate(item: TireQuota): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TireQuota) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TireQuota>): MutableMap<String, MutableList<String>> {
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

fun defaultTireQuotaValidator(): TireQuotaValidator {
    return TireQuotaValidator(
        mutableListOf<TireQuotaRule>(
            TireQuotaRequiredSegmentRule("tire quota needs this field"),
            TireQuotaSegmentLengthRule(86400),
            TireQuotaApprovedGuardRule(255),
            TireQuotaFloorValueRangeRule(0.1, 4987.5),
            TireQuotaDurationMinutesRangeRule(32, 255),
        ),
    )
}
