package com.acme.transport.lastmile.parcel.scheduledparcelreservation

import com.acme.transport.kit.text.normalize

/** A rule a parcel batch must pass. Check returns its problems, or none. */
interface ParcelBatchRule {
    fun check(item: ParcelBatch): MutableList<String>
}

data class ParcelBatchCodeLengthRule(var maxLength: Int) : ParcelBatchRule {
    override fun check(item: ParcelBatch): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ParcelBatchRegionLengthRule(var maxLength: Int) : ParcelBatchRule {
    override fun check(item: ParcelBatch): MutableList<String> {
        val size = item.region.length
        if (size > maxLength) {
            return mutableListOf<String>("region is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ParcelBatchLimitRangeRule(var min: Int, var max: Int) : ParcelBatchRule {
    override fun check(item: ParcelBatch): MutableList<String> {
        val value = item.limit
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("limit $value is below $min")
        }
        if (value > max) {
            problems.add("limit $value is above $max")
        }
        return problems
    }
}

data class ParcelBatchAllowedVehicleIdRule(var allowed: MutableList<String>) : ParcelBatchRule {
    override fun check(item: ParcelBatch): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.vehicleId))) {
            return mutableListOf<String>("vehicle id ${item.vehicleId} is blocked")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a parcel batch. */
data class ParcelBatchValidator(var rules: MutableList<ParcelBatchRule>) {
    fun validate(item: ParcelBatch): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ParcelBatch) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ParcelBatch>): MutableMap<String, MutableList<String>> {
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

fun defaultParcelBatchValidator(): ParcelBatchValidator {
    return ParcelBatchValidator(
        mutableListOf<ParcelBatchRule>(
            ParcelBatchCodeLengthRule(86400),
            ParcelBatchRegionLengthRule(16),
            ParcelBatchLimitRangeRule(16, 1000),
            ParcelBatchAllowedVehicleIdRule(mutableListOf<String>("lenient", "lenient")),
        ),
    )
}
