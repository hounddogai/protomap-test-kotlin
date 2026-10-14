package com.acme.transport.lastmile.parcel.exception

import com.acme.transport.kit.text.normalize

/** A rule a regional parcel review must pass. Check returns its problems, or none. */
interface RegionalParcelReviewRule {
    fun check(item: RegionalParcelReview): MutableList<String>
}

data class RegionalParcelReviewNameLengthRule(var maxLength: Int) : RegionalParcelReviewRule {
    override fun check(item: RegionalParcelReview): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalParcelReviewDepotLengthRule(var maxLength: Int) : RegionalParcelReviewRule {
    override fun check(item: RegionalParcelReview): MutableList<String> {
        val size = item.depot.length
        if (size > maxLength) {
            return mutableListOf<String>("depot is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalParcelReviewReviewerLengthRule(var maxLength: Int) : RegionalParcelReviewRule {
    override fun check(item: RegionalParcelReview): MutableList<String> {
        val size = item.reviewer.length
        if (size > maxLength) {
            return mutableListOf<String>("reviewer is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalParcelReviewRequiredReviewerRule(var message: String) : RegionalParcelReviewRule {
    override fun check(item: RegionalParcelReview): MutableList<String> {
        if (item.reviewer.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalParcelReviewAllowedDepotRule(var allowed: MutableList<String>) : RegionalParcelReviewRule {
    override fun check(item: RegionalParcelReview): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.depot))) {
            return mutableListOf<String>("depot ${item.depot} is below minimum")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional parcel review. */
data class RegionalParcelReviewValidator(var rules: MutableList<RegionalParcelReviewRule>) {
    fun validate(item: RegionalParcelReview): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RegionalParcelReview) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalParcelReview>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalParcelReviewValidator(): RegionalParcelReviewValidator {
    return RegionalParcelReviewValidator(
        mutableListOf<RegionalParcelReviewRule>(
            RegionalParcelReviewNameLengthRule(3600),
            RegionalParcelReviewDepotLengthRule(86400),
            RegionalParcelReviewReviewerLengthRule(16),
            RegionalParcelReviewRequiredReviewerRule("regional parcel review needs an entry"),
            RegionalParcelReviewAllowedDepotRule(mutableListOf<String>("global")),
        ),
    )
}
