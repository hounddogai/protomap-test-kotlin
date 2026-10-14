package com.acme.transport.customs.declaration.plan

import com.acme.transport.kit.text.normalize

/** A rule a weighted declaration review must pass. Check returns its problems, or none. */
interface WeightedDeclarationReviewRule {
    fun check(item: WeightedDeclarationReview): MutableList<String>
}

data class WeightedDeclarationReviewNameLengthRule(var maxLength: Int) : WeightedDeclarationReviewRule {
    override fun check(item: WeightedDeclarationReview): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedDeclarationReviewAllowedCodeRule(
    var allowed: MutableList<String>,
) : WeightedDeclarationReviewRule {
    override fun check(item: WeightedDeclarationReview): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class WeightedDeclarationReviewRequiredCodeRule(var message: String) : WeightedDeclarationReviewRule {
    override fun check(item: WeightedDeclarationReview): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedDeclarationReviewOwnerIdLengthRule(var maxLength: Int) : WeightedDeclarationReviewRule {
    override fun check(item: WeightedDeclarationReview): MutableList<String> {
        val size = item.ownerId.length
        if (size > maxLength) {
            return mutableListOf<String>("owner id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedDeclarationReviewCodePrefixRule(var prefix: String) : WeightedDeclarationReviewRule {
    override fun check(item: WeightedDeclarationReview): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class WeightedDeclarationReviewRequiredOwnerIdRule(var message: String) : WeightedDeclarationReviewRule {
    override fun check(item: WeightedDeclarationReview): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted declaration review. */
data class WeightedDeclarationReviewValidator(var rules: MutableList<WeightedDeclarationReviewRule>) {
    fun validate(item: WeightedDeclarationReview): MutableList<String> {
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

    fun requireValid(item: WeightedDeclarationReview) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedDeclarationReview>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedDeclarationReviewValidator(): WeightedDeclarationReviewValidator {
    return WeightedDeclarationReviewValidator(
        mutableListOf<WeightedDeclarationReviewRule>(
            WeightedDeclarationReviewNameLengthRule(16),
            WeightedDeclarationReviewAllowedCodeRule(mutableListOf<String>()),
            WeightedDeclarationReviewRequiredCodeRule("weighted declaration review needs a value"),
            WeightedDeclarationReviewOwnerIdLengthRule(1000),
            WeightedDeclarationReviewCodePrefixRule("ACM"),
            WeightedDeclarationReviewRequiredOwnerIdRule("weighted declaration review needs an entry"),
        ),
    )
}
