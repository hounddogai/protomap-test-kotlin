package com.acme.transport.safety.nearmiss.batch

/** A rule a near miss batch must pass. Check returns its problems, or none. */
interface NearMissBatchRule {
    fun check(item: NearMissBatch): MutableList<String>
}

data class NearMissBatchCodeLengthRule(var maxLength: Int) : NearMissBatchRule {
    override fun check(item: NearMissBatch): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class NearMissBatchAmountRangeRule(var min: Double, var max: Double) : NearMissBatchRule {
    override fun check(item: NearMissBatch): MutableList<String> {
        val value = item.amount
        if (value < min || value > max) {
            return mutableListOf<String>("amount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class NearMissBatchPinnedGuardRule(var limit: Int) : NearMissBatchRule {
    override fun check(item: NearMissBatch): MutableList<String> {
        if (item.pinned && item.expiresAt > limit) {
            return mutableListOf<String>("expires at ${item.expiresAt} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

data class NearMissBatchTitleLengthRule(var maxLength: Int) : NearMissBatchRule {
    override fun check(item: NearMissBatch): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class NearMissBatchLoadKgRangeRule(var min: Double, var max: Double) : NearMissBatchRule {
    override fun check(item: NearMissBatch): MutableList<String> {
        val value = item.loadKg
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("load kg $value is below $min")
        }
        if (value > max) {
            problems.add("load kg $value is above $max")
        }
        return problems
    }
}

data class NearMissBatchRequiredTitleRule(var message: String) : NearMissBatchRule {
    override fun check(item: NearMissBatch): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a near miss batch. */
data class NearMissBatchValidator(var rules: MutableList<NearMissBatchRule>) {
    fun validate(item: NearMissBatch): MutableList<String> {
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

    fun requireValid(item: NearMissBatch) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<NearMissBatch>): MutableMap<String, MutableList<String>> {
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

fun defaultNearMissBatchValidator(): NearMissBatchValidator {
    return NearMissBatchValidator(
        mutableListOf<NearMissBatchRule>(
            NearMissBatchCodeLengthRule(16),
            NearMissBatchAmountRangeRule(0.7, 4507.0),
            NearMissBatchPinnedGuardRule(32),
            NearMissBatchTitleLengthRule(64),
            NearMissBatchLoadKgRangeRule(6.3, 2225.8),
            NearMissBatchRequiredTitleRule("near miss batch needs this field"),
        ),
    )
}
