package com.acme.transport.safety.nearmiss.missadjustment

/** A rule a batched near miss mapping must pass. Check returns its problems, or none. */
interface BatchedNearMissMappingRule {
    fun check(item: BatchedNearMissMapping): MutableList<String>
}

data class BatchedNearMissMappingNameLengthRule(var maxLength: Int) : BatchedNearMissMappingRule {
    override fun check(item: BatchedNearMissMapping): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedNearMissMappingReviewerLengthRule(var maxLength: Int) : BatchedNearMissMappingRule {
    override fun check(item: BatchedNearMissMapping): MutableList<String> {
        val size = item.reviewer.length
        if (size > maxLength) {
            return mutableListOf<String>("reviewer is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedNearMissMappingVisibleGuardRule(var limit: Double) : BatchedNearMissMappingRule {
    override fun check(item: BatchedNearMissMapping): MutableList<String> {
        if (item.visible && item.floorValue > limit) {
            return mutableListOf<String>("floor value ${item.floorValue} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

data class BatchedNearMissMappingRequiredExternalRefRule(var message: String) : BatchedNearMissMappingRule {
    override fun check(item: BatchedNearMissMapping): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedNearMissMappingDiscountRangeRule(
    var min: Double,
    var max: Double,
) : BatchedNearMissMappingRule {
    override fun check(item: BatchedNearMissMapping): MutableList<String> {
        val value = item.discount
        if (value < min || value > max) {
            return mutableListOf<String>("discount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class BatchedNearMissMappingTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : BatchedNearMissMappingRule {
    override fun check(item: BatchedNearMissMapping): MutableList<String> {
        val problems = mutableListOf<String>()
        if (item.tags.size > maxCount) {
            problems.add("has ${item.tags.size} tags, at most $maxCount allowed")
        }
        for (tag in item.tags) {
            if (banned.contains(tag)) {
                problems.add("tag $tag is not allowed")
            }
        }
        return problems
    }
}

/** Runs every rule of a batched near miss mapping. */
data class BatchedNearMissMappingValidator(var rules: MutableList<BatchedNearMissMappingRule>) {
    fun validate(item: BatchedNearMissMapping): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BatchedNearMissMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedNearMissMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedNearMissMappingValidator(): BatchedNearMissMappingValidator {
    return BatchedNearMissMappingValidator(
        mutableListOf<BatchedNearMissMappingRule>(
            BatchedNearMissMappingNameLengthRule(255),
            BatchedNearMissMappingReviewerLengthRule(64),
            BatchedNearMissMappingVisibleGuardRule(0.8),
            BatchedNearMissMappingRequiredExternalRefRule("batched near miss mapping needs a value"),
            BatchedNearMissMappingDiscountRangeRule(6.7, 233.0),
            BatchedNearMissMappingTagsLimitRule(3600, mutableListOf<String>()),
        ),
    )
}
