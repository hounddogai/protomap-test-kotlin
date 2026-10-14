package com.acme.transport.linehaul.trailer.batch

/** A rule a trailer batch must pass. Check returns its problems, or none. */
interface TrailerBatchRule {
    fun check(item: TrailerBatch): MutableList<String>
}

data class TrailerBatchGraceDaysRangeRule(var min: Int, var max: Int) : TrailerBatchRule {
    override fun check(item: TrailerBatch): MutableList<String> {
        val value = item.graceDays
        if (value < min || value > max) {
            return mutableListOf<String>("grace days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TrailerBatchCreatedAtRangeRule(var min: Int, var max: Int) : TrailerBatchRule {
    override fun check(item: TrailerBatch): MutableList<String> {
        val value = item.createdAt
        if (value < min || value > max) {
            return mutableListOf<String>("created at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TrailerBatchCodeLengthRule(var maxLength: Int) : TrailerBatchRule {
    override fun check(item: TrailerBatch): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TrailerBatchWeightRangeRule(var min: Double, var max: Double) : TrailerBatchRule {
    override fun check(item: TrailerBatch): MutableList<String> {
        val value = item.weight
        if (value < min || value > max) {
            return mutableListOf<String>("weight $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TrailerBatchRequiredPhoneRule(var message: String) : TrailerBatchRule {
    override fun check(item: TrailerBatch): MutableList<String> {
        if (item.phone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TrailerBatchPhoneLengthRule(var maxLength: Int) : TrailerBatchRule {
    override fun check(item: TrailerBatch): MutableList<String> {
        val size = item.phone.length
        if (size > maxLength) {
            return mutableListOf<String>("phone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a trailer batch. */
data class TrailerBatchValidator(var rules: MutableList<TrailerBatchRule>) {
    fun validate(item: TrailerBatch): MutableList<String> {
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

    fun requireValid(item: TrailerBatch) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TrailerBatch>): MutableMap<String, MutableList<String>> {
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

fun defaultTrailerBatchValidator(): TrailerBatchValidator {
    return TrailerBatchValidator(
        mutableListOf<TrailerBatchRule>(
            TrailerBatchGraceDaysRangeRule(86400, 86400),
            TrailerBatchCreatedAtRangeRule(16, 64),
            TrailerBatchCodeLengthRule(3600),
            TrailerBatchWeightRangeRule(0.5, 4888.0),
            TrailerBatchRequiredPhoneRule("trailer batch needs this field"),
            TrailerBatchPhoneLengthRule(1000),
        ),
    )
}
