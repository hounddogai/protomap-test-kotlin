package com.acme.transport.lastmile.drop.profile

/** A rule a batched drop profile must pass. Check returns its problems, or none. */
interface BatchedDropProfileRule {
    fun check(item: BatchedDropProfile): MutableList<String>
}

data class BatchedDropProfileCodePrefixRule(var prefix: String) : BatchedDropProfileRule {
    override fun check(item: BatchedDropProfile): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class BatchedDropProfileLabelLengthRule(var maxLength: Int) : BatchedDropProfileRule {
    override fun check(item: BatchedDropProfile): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedDropProfileRequiredLabelRule(var message: String) : BatchedDropProfileRule {
    override fun check(item: BatchedDropProfile): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedDropProfileScoreRangeRule(var min: Double, var max: Double) : BatchedDropProfileRule {
    override fun check(item: BatchedDropProfile): MutableList<String> {
        val value = item.score
        if (value < min || value > max) {
            return mutableListOf<String>("score $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class BatchedDropProfilePinnedGuardRule(var limit: Double) : BatchedDropProfileRule {
    override fun check(item: BatchedDropProfile): MutableList<String> {
        if (item.pinned && item.score > limit) {
            return mutableListOf<String>("score ${item.score} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

data class BatchedDropProfileDescriptionLengthRule(var maxLength: Int) : BatchedDropProfileRule {
    override fun check(item: BatchedDropProfile): MutableList<String> {
        val size = item.description.length
        if (size > maxLength) {
            return mutableListOf<String>("description is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched drop profile. */
data class BatchedDropProfileValidator(var rules: MutableList<BatchedDropProfileRule>) {
    fun validate(item: BatchedDropProfile): MutableList<String> {
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

    fun requireValid(item: BatchedDropProfile) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedDropProfile>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedDropProfileValidator(): BatchedDropProfileValidator {
    return BatchedDropProfileValidator(
        mutableListOf<BatchedDropProfileRule>(
            BatchedDropProfileCodePrefixRule("REF"),
            BatchedDropProfileLabelLengthRule(255),
            BatchedDropProfileRequiredLabelRule("batched drop profile needs this field"),
            BatchedDropProfileScoreRangeRule(2.4, 3014.9),
            BatchedDropProfilePinnedGuardRule(8.5),
            BatchedDropProfileDescriptionLengthRule(80),
        ),
    )
}
