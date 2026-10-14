package com.acme.transport.customs.hold.batch

/** A rule a hold exception must pass. Check returns its problems, or none. */
interface HoldExceptionRule {
    fun check(item: HoldException): MutableList<String>
}

data class HoldExceptionCodeLengthRule(var maxLength: Int) : HoldExceptionRule {
    override fun check(item: HoldException): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class HoldExceptionThresholdRangeRule(var min: Double, var max: Double) : HoldExceptionRule {
    override fun check(item: HoldException): MutableList<String> {
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

data class HoldExceptionVehicleIdLengthRule(var maxLength: Int) : HoldExceptionRule {
    override fun check(item: HoldException): MutableList<String> {
        val size = item.vehicleId.length
        if (size > maxLength) {
            return mutableListOf<String>("vehicle id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a hold exception. */
data class HoldExceptionValidator(var rules: MutableList<HoldExceptionRule>) {
    fun validate(item: HoldException): MutableList<String> {
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

    fun requireValid(item: HoldException) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<HoldException>): MutableMap<String, MutableList<String>> {
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

fun defaultHoldExceptionValidator(): HoldExceptionValidator {
    return HoldExceptionValidator(
        mutableListOf<HoldExceptionRule>(
            HoldExceptionCodeLengthRule(120),
            HoldExceptionThresholdRangeRule(4.6, 3139.3),
            HoldExceptionVehicleIdLengthRule(32),
        ),
    )
}
