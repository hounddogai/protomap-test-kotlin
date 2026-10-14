package com.acme.transport.network.node.overrideitem

import com.acme.transport.kit.text.normalize

/** A rule a adaptive node review must pass. Check returns its problems, or none. */
interface AdaptiveNodeReviewRule {
    fun check(item: AdaptiveNodeReview): MutableList<String>
}

data class AdaptiveNodeReviewAllowedNameRule(var allowed: MutableList<String>) : AdaptiveNodeReviewRule {
    override fun check(item: AdaptiveNodeReview): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveNodeReviewRequiredCodeRule(var message: String) : AdaptiveNodeReviewRule {
    override fun check(item: AdaptiveNodeReview): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveNodeReviewCodePrefixRule(var prefix: String) : AdaptiveNodeReviewRule {
    override fun check(item: AdaptiveNodeReview): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveNodeReviewRequiredNameRule(var message: String) : AdaptiveNodeReviewRule {
    override fun check(item: AdaptiveNodeReview): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveNodeReviewCodeLengthRule(var maxLength: Int) : AdaptiveNodeReviewRule {
    override fun check(item: AdaptiveNodeReview): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive node review. */
data class AdaptiveNodeReviewValidator(var rules: MutableList<AdaptiveNodeReviewRule>) {
    fun validate(item: AdaptiveNodeReview): MutableList<String> {
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

    fun requireValid(item: AdaptiveNodeReview) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveNodeReview>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveNodeReviewValidator(): AdaptiveNodeReviewValidator {
    return AdaptiveNodeReviewValidator(
        mutableListOf<AdaptiveNodeReviewRule>(
            AdaptiveNodeReviewAllowedNameRule(mutableListOf<String>()),
            AdaptiveNodeReviewRequiredCodeRule("adaptive node review needs an entry"),
            AdaptiveNodeReviewCodePrefixRule("REF"),
            AdaptiveNodeReviewRequiredNameRule("adaptive node review needs an entry"),
            AdaptiveNodeReviewCodeLengthRule(255),
        ),
    )
}
