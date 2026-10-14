package com.acme.transport.linehaul.backhaul.request

import com.acme.transport.kit.text.normalize

/** A rule a rolling backhaul review must pass. Check returns its problems, or none. */
interface RollingBackhaulReviewRule {
    fun check(item: RollingBackhaulReview): MutableList<String>
}

data class RollingBackhaulReviewAllowedCodeRule(var allowed: MutableList<String>) : RollingBackhaulReviewRule {
    override fun check(item: RollingBackhaulReview): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class RollingBackhaulReviewRequiredNameRule(var message: String) : RollingBackhaulReviewRule {
    override fun check(item: RollingBackhaulReview): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingBackhaulReviewRequiredCategoryRule(var message: String) : RollingBackhaulReviewRule {
    override fun check(item: RollingBackhaulReview): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingBackhaulReviewCodeLengthRule(var maxLength: Int) : RollingBackhaulReviewRule {
    override fun check(item: RollingBackhaulReview): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling backhaul review. */
data class RollingBackhaulReviewValidator(var rules: MutableList<RollingBackhaulReviewRule>) {
    fun validate(item: RollingBackhaulReview): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RollingBackhaulReview) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingBackhaulReview>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingBackhaulReviewValidator(): RollingBackhaulReviewValidator {
    return RollingBackhaulReviewValidator(
        mutableListOf<RollingBackhaulReviewRule>(
            RollingBackhaulReviewAllowedCodeRule(mutableListOf<String>()),
            RollingBackhaulReviewRequiredNameRule("rolling backhaul review needs this field"),
            RollingBackhaulReviewRequiredCategoryRule("rolling backhaul review needs an entry"),
            RollingBackhaulReviewCodeLengthRule(80),
        ),
    )
}
