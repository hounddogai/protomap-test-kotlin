package com.acme.transport.network.servicearea.areacycle

import com.acme.transport.kit.text.normalize

/** A rule a batched service area cycle must pass. Check returns its problems, or none. */
interface BatchedServiceAreaCycleRule {
    fun check(item: BatchedServiceAreaCycle): MutableList<String>
}

data class BatchedServiceAreaCycleGraceDaysRangeRule(var min: Int, var max: Int) : BatchedServiceAreaCycleRule {
    override fun check(item: BatchedServiceAreaCycle): MutableList<String> {
        val value = item.graceDays
        if (value < min || value > max) {
            return mutableListOf<String>("grace days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class BatchedServiceAreaCycleChecksumLengthRule(var maxLength: Int) : BatchedServiceAreaCycleRule {
    override fun check(item: BatchedServiceAreaCycle): MutableList<String> {
        val size = item.checksum.length
        if (size > maxLength) {
            return mutableListOf<String>("checksum is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedServiceAreaCycleRequiredShiftCodeRule(var message: String) : BatchedServiceAreaCycleRule {
    override fun check(item: BatchedServiceAreaCycle): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedServiceAreaCycleShiftCodeLengthRule(var maxLength: Int) : BatchedServiceAreaCycleRule {
    override fun check(item: BatchedServiceAreaCycle): MutableList<String> {
        val size = item.shiftCode.length
        if (size > maxLength) {
            return mutableListOf<String>("shift code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedServiceAreaCycleAllowedNameRule(
    var allowed: MutableList<String>,
) : BatchedServiceAreaCycleRule {
    override fun check(item: BatchedServiceAreaCycle): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class BatchedServiceAreaCycleCodeLengthRule(var maxLength: Int) : BatchedServiceAreaCycleRule {
    override fun check(item: BatchedServiceAreaCycle): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched service area cycle. */
data class BatchedServiceAreaCycleValidator(var rules: MutableList<BatchedServiceAreaCycleRule>) {
    fun validate(item: BatchedServiceAreaCycle): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BatchedServiceAreaCycle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedServiceAreaCycle>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedServiceAreaCycleValidator(): BatchedServiceAreaCycleValidator {
    return BatchedServiceAreaCycleValidator(
        mutableListOf<BatchedServiceAreaCycleRule>(
            BatchedServiceAreaCycleGraceDaysRangeRule(120, 1000),
            BatchedServiceAreaCycleChecksumLengthRule(255),
            BatchedServiceAreaCycleRequiredShiftCodeRule("batched service area cycle needs a value"),
            BatchedServiceAreaCycleShiftCodeLengthRule(255),
            BatchedServiceAreaCycleAllowedNameRule(mutableListOf<String>("tiered")),
            BatchedServiceAreaCycleCodeLengthRule(32),
        ),
    )
}
