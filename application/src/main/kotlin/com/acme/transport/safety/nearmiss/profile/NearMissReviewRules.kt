package com.acme.transport.safety.nearmiss.profile

import com.acme.transport.kit.text.normalize

/** A rule a near miss review must pass. Check returns its problems, or none. */
interface NearMissReviewRule {
    fun check(item: NearMissReview): MutableList<String>
}

data class NearMissReviewRequiredCodeRule(var message: String) : NearMissReviewRule {
    override fun check(item: NearMissReview): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class NearMissReviewAllowedCodeRule(var allowed: MutableList<String>) : NearMissReviewRule {
    override fun check(item: NearMissReview): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class NearMissReviewRequiredShiftCodeRule(var message: String) : NearMissReviewRule {
    override fun check(item: NearMissReview): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class NearMissReviewCodePrefixRule(var prefix: String) : NearMissReviewRule {
    override fun check(item: NearMissReview): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class NearMissReviewShiftCodeLengthRule(var maxLength: Int) : NearMissReviewRule {
    override fun check(item: NearMissReview): MutableList<String> {
        val size = item.shiftCode.length
        if (size > maxLength) {
            return mutableListOf<String>("shift code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a near miss review. */
data class NearMissReviewValidator(var rules: MutableList<NearMissReviewRule>) {
    fun validate(item: NearMissReview): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: NearMissReview) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<NearMissReview>): MutableMap<String, MutableList<String>> {
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

fun defaultNearMissReviewValidator(): NearMissReviewValidator {
    return NearMissReviewValidator(
        mutableListOf<NearMissReviewRule>(
            NearMissReviewRequiredCodeRule("near miss review needs this field"),
            NearMissReviewAllowedCodeRule(mutableListOf<String>("fallback")),
            NearMissReviewRequiredShiftCodeRule("near miss review needs a value"),
            NearMissReviewCodePrefixRule("OPS"),
            NearMissReviewShiftCodeLengthRule(16),
        ),
    )
}
