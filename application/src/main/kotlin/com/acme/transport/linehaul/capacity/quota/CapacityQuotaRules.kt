package com.acme.transport.linehaul.capacity.quota

/** A rule a capacity quota must pass. Check returns its problems, or none. */
interface CapacityQuotaRule {
    fun check(item: CapacityQuota): MutableList<String>
}

data class CapacityQuotaSegmentLengthRule(var maxLength: Int) : CapacityQuotaRule {
    override fun check(item: CapacityQuota): MutableList<String> {
        val size = item.segment.length
        if (size > maxLength) {
            return mutableListOf<String>("segment is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CapacityQuotaFuelLitersRangeRule(var min: Double, var max: Double) : CapacityQuotaRule {
    override fun check(item: CapacityQuota): MutableList<String> {
        val value = item.fuelLiters
        if (value < min || value > max) {
            return mutableListOf<String>("fuel liters $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CapacityQuotaTitleLengthRule(var maxLength: Int) : CapacityQuotaRule {
    override fun check(item: CapacityQuota): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CapacityQuotaThresholdRangeRule(var min: Double, var max: Double) : CapacityQuotaRule {
    override fun check(item: CapacityQuota): MutableList<String> {
        val value = item.threshold
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("threshold $value is below $min")
        }
        if (value > max) {
            problems.add("threshold $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a capacity quota. */
data class CapacityQuotaValidator(var rules: MutableList<CapacityQuotaRule>) {
    fun validate(item: CapacityQuota): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CapacityQuota) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CapacityQuota>): MutableMap<String, MutableList<String>> {
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

fun defaultCapacityQuotaValidator(): CapacityQuotaValidator {
    return CapacityQuotaValidator(
        mutableListOf<CapacityQuotaRule>(
            CapacityQuotaSegmentLengthRule(16),
            CapacityQuotaFuelLitersRangeRule(7.9, 2972.4),
            CapacityQuotaTitleLengthRule(16),
            CapacityQuotaThresholdRangeRule(6.9, 4147.0),
        ),
    )
}
