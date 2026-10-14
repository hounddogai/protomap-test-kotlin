package com.acme.transport.safety.violation.proposal

/** A rule a violation review must pass. Check returns its problems, or none. */
interface ViolationReviewRule {
    fun check(item: ViolationReview): MutableList<String>
}

data class ViolationReviewChecksumLengthRule(var maxLength: Int) : ViolationReviewRule {
    override fun check(item: ViolationReview): MutableList<String> {
        val size = item.checksum.length
        if (size > maxLength) {
            return mutableListOf<String>("checksum is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ViolationReviewRequiredNameRule(var message: String) : ViolationReviewRule {
    override fun check(item: ViolationReview): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ViolationReviewExpiresAtRangeRule(var min: Int, var max: Int) : ViolationReviewRule {
    override fun check(item: ViolationReview): MutableList<String> {
        val value = item.expiresAt
        if (value < min || value > max) {
            return mutableListOf<String>("expires at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ViolationReviewCarrierLengthRule(var maxLength: Int) : ViolationReviewRule {
    override fun check(item: ViolationReview): MutableList<String> {
        val size = item.carrier.length
        if (size > maxLength) {
            return mutableListOf<String>("carrier is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ViolationReviewRequiredChecksumRule(var message: String) : ViolationReviewRule {
    override fun check(item: ViolationReview): MutableList<String> {
        if (item.checksum.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ViolationReviewNameLengthRule(var maxLength: Int) : ViolationReviewRule {
    override fun check(item: ViolationReview): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a violation review. */
data class ViolationReviewValidator(var rules: MutableList<ViolationReviewRule>) {
    fun validate(item: ViolationReview): MutableList<String> {
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

    fun requireValid(item: ViolationReview) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ViolationReview>): MutableMap<String, MutableList<String>> {
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

fun defaultViolationReviewValidator(): ViolationReviewValidator {
    return ViolationReviewValidator(
        mutableListOf<ViolationReviewRule>(
            ViolationReviewChecksumLengthRule(64),
            ViolationReviewRequiredNameRule("violation review needs an entry"),
            ViolationReviewExpiresAtRangeRule(64, 3600),
            ViolationReviewCarrierLengthRule(32),
            ViolationReviewRequiredChecksumRule("violation review needs this field"),
            ViolationReviewNameLengthRule(64),
        ),
    )
}
