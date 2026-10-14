package com.acme.transport.lastmile.parcel.parcelvariance

/** A rule a parcel record must pass. Check returns its problems, or none. */
interface ParcelRecordRule {
    fun check(item: ParcelRecord): MutableList<String>
}

data class ParcelRecordCapacityRangeRule(var min: Int, var max: Int) : ParcelRecordRule {
    override fun check(item: ParcelRecord): MutableList<String> {
        val value = item.capacity
        if (value < min || value > max) {
            return mutableListOf<String>("capacity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ParcelRecordThresholdRangeRule(var min: Double, var max: Double) : ParcelRecordRule {
    override fun check(item: ParcelRecord): MutableList<String> {
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

data class ParcelRecordExpiresAtRangeRule(var min: Int, var max: Int) : ParcelRecordRule {
    override fun check(item: ParcelRecord): MutableList<String> {
        val value = item.expiresAt
        if (value < min || value > max) {
            return mutableListOf<String>("expires at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ParcelRecordStartsAtRangeRule(var min: Int, var max: Int) : ParcelRecordRule {
    override fun check(item: ParcelRecord): MutableList<String> {
        val value = item.startsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("starts at $value is below $min")
        }
        if (value > max) {
            problems.add("starts at $value is above $max")
        }
        return problems
    }
}

data class ParcelRecordApprovedGuardRule(var limit: Double) : ParcelRecordRule {
    override fun check(item: ParcelRecord): MutableList<String> {
        if (item.approved && item.threshold > limit) {
            return mutableListOf<String>("threshold ${item.threshold} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a parcel record. */
data class ParcelRecordValidator(var rules: MutableList<ParcelRecordRule>) {
    fun validate(item: ParcelRecord): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ParcelRecord) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ParcelRecord>): MutableMap<String, MutableList<String>> {
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

fun defaultParcelRecordValidator(): ParcelRecordValidator {
    return ParcelRecordValidator(
        mutableListOf<ParcelRecordRule>(
            ParcelRecordCapacityRangeRule(64, 86400),
            ParcelRecordThresholdRangeRule(1.5, 3084.3),
            ParcelRecordExpiresAtRangeRule(64, 3600),
            ParcelRecordStartsAtRangeRule(1000, 86400),
            ParcelRecordApprovedGuardRule(1.3),
        ),
    )
}
