package com.acme.transport.linehaul.relay.group

/** A rule a capped relay review must pass. Check returns its problems, or none. */
interface CappedRelayReviewRule {
    fun check(item: CappedRelayReview): MutableList<String>
}

data class CappedRelayReviewRequiredCodeRule(var message: String) : CappedRelayReviewRule {
    override fun check(item: CappedRelayReview): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedRelayReviewCodePrefixRule(var prefix: String) : CappedRelayReviewRule {
    override fun check(item: CappedRelayReview): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class CappedRelayReviewCapacityRangeRule(var min: Int, var max: Int) : CappedRelayReviewRule {
    override fun check(item: CappedRelayReview): MutableList<String> {
        val value = item.capacity
        if (value < min || value > max) {
            return mutableListOf<String>("capacity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped relay review. */
data class CappedRelayReviewValidator(var rules: MutableList<CappedRelayReviewRule>) {
    fun validate(item: CappedRelayReview): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CappedRelayReview) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedRelayReview>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedRelayReviewValidator(): CappedRelayReviewValidator {
    return CappedRelayReviewValidator(
        mutableListOf<CappedRelayReviewRule>(
            CappedRelayReviewRequiredCodeRule("capped relay review needs an entry"),
            CappedRelayReviewCodePrefixRule("REF"),
            CappedRelayReviewCapacityRangeRule(16, 120),
        ),
    )
}
