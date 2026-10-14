package com.acme.transport.crewing.qualification.template

/** A rule a batched qualification cycle must pass. Check returns its problems, or none. */
interface BatchedQualificationCycleRule {
    fun check(item: BatchedQualificationCycle): MutableList<String>
}

data class BatchedQualificationCycleRequiredTrailerIdRule(var message: String) : BatchedQualificationCycleRule {
    override fun check(item: BatchedQualificationCycle): MutableList<String> {
        if (item.trailerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedQualificationCycleCodeLengthRule(var maxLength: Int) : BatchedQualificationCycleRule {
    override fun check(item: BatchedQualificationCycle): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedQualificationCycleGraceDaysRangeRule(
    var min: Int,
    var max: Int,
) : BatchedQualificationCycleRule {
    override fun check(item: BatchedQualificationCycle): MutableList<String> {
        val value = item.graceDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("grace days $value is below $min")
        }
        if (value > max) {
            problems.add("grace days $value is above $max")
        }
        return problems
    }
}

data class BatchedQualificationCycleStartsAtRangeRule(
    var min: Int,
    var max: Int,
) : BatchedQualificationCycleRule {
    override fun check(item: BatchedQualificationCycle): MutableList<String> {
        val value = item.startsAt
        if (value < min || value > max) {
            return mutableListOf<String>("starts at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class BatchedQualificationCycleRequiredPhoneRule(var message: String) : BatchedQualificationCycleRule {
    override fun check(item: BatchedQualificationCycle): MutableList<String> {
        if (item.phone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched qualification cycle. */
data class BatchedQualificationCycleValidator(var rules: MutableList<BatchedQualificationCycleRule>) {
    fun validate(item: BatchedQualificationCycle): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BatchedQualificationCycle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedQualificationCycle>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedQualificationCycleValidator(): BatchedQualificationCycleValidator {
    return BatchedQualificationCycleValidator(
        mutableListOf<BatchedQualificationCycleRule>(
            BatchedQualificationCycleRequiredTrailerIdRule("batched qualification cycle needs an entry"),
            BatchedQualificationCycleCodeLengthRule(3600),
            BatchedQualificationCycleGraceDaysRangeRule(120, 255),
            BatchedQualificationCycleStartsAtRangeRule(16, 120),
            BatchedQualificationCycleRequiredPhoneRule("batched qualification cycle needs an entry"),
        ),
    )
}
