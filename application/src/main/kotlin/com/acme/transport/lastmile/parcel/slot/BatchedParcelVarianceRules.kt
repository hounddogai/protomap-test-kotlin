package com.acme.transport.lastmile.parcel.slot

/** A rule a batched parcel variance must pass. Check returns its problems, or none. */
interface BatchedParcelVarianceRule {
    fun check(item: BatchedParcelVariance): MutableList<String>
}

data class BatchedParcelVarianceNameLengthRule(var maxLength: Int) : BatchedParcelVarianceRule {
    override fun check(item: BatchedParcelVariance): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedParcelVarianceRevisionRangeRule(var min: Int, var max: Int) : BatchedParcelVarianceRule {
    override fun check(item: BatchedParcelVariance): MutableList<String> {
        val value = item.revision
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("revision $value is below $min")
        }
        if (value > max) {
            problems.add("revision $value is above $max")
        }
        return problems
    }
}

data class BatchedParcelVarianceAttemptsRangeRule(var min: Int, var max: Int) : BatchedParcelVarianceRule {
    override fun check(item: BatchedParcelVariance): MutableList<String> {
        val value = item.attempts
        if (value < min || value > max) {
            return mutableListOf<String>("attempts $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class BatchedParcelVarianceCodeLengthRule(var maxLength: Int) : BatchedParcelVarianceRule {
    override fun check(item: BatchedParcelVariance): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedParcelVarianceRequiredNameRule(var message: String) : BatchedParcelVarianceRule {
    override fun check(item: BatchedParcelVariance): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedParcelVarianceDriverIdLengthRule(var maxLength: Int) : BatchedParcelVarianceRule {
    override fun check(item: BatchedParcelVariance): MutableList<String> {
        val size = item.driverId.length
        if (size > maxLength) {
            return mutableListOf<String>("driver id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched parcel variance. */
data class BatchedParcelVarianceValidator(var rules: MutableList<BatchedParcelVarianceRule>) {
    fun validate(item: BatchedParcelVariance): MutableList<String> {
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

    fun requireValid(item: BatchedParcelVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedParcelVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedParcelVarianceValidator(): BatchedParcelVarianceValidator {
    return BatchedParcelVarianceValidator(
        mutableListOf<BatchedParcelVarianceRule>(
            BatchedParcelVarianceNameLengthRule(3600),
            BatchedParcelVarianceRevisionRangeRule(120, 255),
            BatchedParcelVarianceAttemptsRangeRule(86400, 86400),
            BatchedParcelVarianceCodeLengthRule(1000),
            BatchedParcelVarianceRequiredNameRule("batched parcel variance needs this field"),
            BatchedParcelVarianceDriverIdLengthRule(64),
        ),
    )
}
