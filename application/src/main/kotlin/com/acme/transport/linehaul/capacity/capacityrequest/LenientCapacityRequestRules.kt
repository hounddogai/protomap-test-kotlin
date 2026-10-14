package com.acme.transport.linehaul.capacity.capacityrequest

import com.acme.transport.kit.text.normalize

/** A rule a lenient capacity request must pass. Check returns its problems, or none. */
interface LenientCapacityRequestRule {
    fun check(item: LenientCapacityRequest): MutableList<String>
}

data class LenientCapacityRequestRequiredShiftCodeRule(var message: String) : LenientCapacityRequestRule {
    override fun check(item: LenientCapacityRequest): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientCapacityRequestTitleLengthRule(var maxLength: Int) : LenientCapacityRequestRule {
    override fun check(item: LenientCapacityRequest): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientCapacityRequestShiftCodeLengthRule(var maxLength: Int) : LenientCapacityRequestRule {
    override fun check(item: LenientCapacityRequest): MutableList<String> {
        val size = item.shiftCode.length
        if (size > maxLength) {
            return mutableListOf<String>("shift code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientCapacityRequestDistanceKmRangeRule(
    var min: Double,
    var max: Double,
) : LenientCapacityRequestRule {
    override fun check(item: LenientCapacityRequest): MutableList<String> {
        val value = item.distanceKm
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("distance km $value is below $min")
        }
        if (value > max) {
            problems.add("distance km $value is above $max")
        }
        return problems
    }
}

data class LenientCapacityRequestCodeLengthRule(var maxLength: Int) : LenientCapacityRequestRule {
    override fun check(item: LenientCapacityRequest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientCapacityRequestAllowedShiftCodeRule(
    var allowed: MutableList<String>,
) : LenientCapacityRequestRule {
    override fun check(item: LenientCapacityRequest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.shiftCode))) {
            return mutableListOf<String>("shift code ${item.shiftCode} is below minimum")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient capacity request. */
data class LenientCapacityRequestValidator(var rules: MutableList<LenientCapacityRequestRule>) {
    fun validate(item: LenientCapacityRequest): MutableList<String> {
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

    fun requireValid(item: LenientCapacityRequest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientCapacityRequest>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientCapacityRequestValidator(): LenientCapacityRequestValidator {
    return LenientCapacityRequestValidator(
        mutableListOf<LenientCapacityRequestRule>(
            LenientCapacityRequestRequiredShiftCodeRule("lenient capacity request needs this field"),
            LenientCapacityRequestTitleLengthRule(32),
            LenientCapacityRequestShiftCodeLengthRule(80),
            LenientCapacityRequestDistanceKmRangeRule(10.0, 2117.3),
            LenientCapacityRequestCodeLengthRule(80),
            LenientCapacityRequestAllowedShiftCodeRule(mutableListOf<String>("capped")),
        ),
    )
}
