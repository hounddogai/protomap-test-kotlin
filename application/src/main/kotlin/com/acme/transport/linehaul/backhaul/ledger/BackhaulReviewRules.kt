package com.acme.transport.linehaul.backhaul.ledger

import com.acme.transport.kit.text.normalize

/** A rule a backhaul review must pass. Check returns its problems, or none. */
interface BackhaulReviewRule {
    fun check(item: BackhaulReview): MutableList<String>
}

data class BackhaulReviewFloorValueRangeRule(var min: Double, var max: Double) : BackhaulReviewRule {
    override fun check(item: BackhaulReview): MutableList<String> {
        val value = item.floorValue
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("floor value $value is below $min")
        }
        if (value > max) {
            problems.add("floor value $value is above $max")
        }
        return problems
    }
}

data class BackhaulReviewCodePrefixRule(var prefix: String) : BackhaulReviewRule {
    override fun check(item: BackhaulReview): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class BackhaulReviewAllowedCodeRule(var allowed: MutableList<String>) : BackhaulReviewRule {
    override fun check(item: BackhaulReview): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is duplicated")
        }
        return mutableListOf<String>()
    }
}

data class BackhaulReviewRequiredTitleRule(var message: String) : BackhaulReviewRule {
    override fun check(item: BackhaulReview): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a backhaul review. */
data class BackhaulReviewValidator(var rules: MutableList<BackhaulReviewRule>) {
    fun validate(item: BackhaulReview): MutableList<String> {
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

    fun requireValid(item: BackhaulReview) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BackhaulReview>): MutableMap<String, MutableList<String>> {
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

fun defaultBackhaulReviewValidator(): BackhaulReviewValidator {
    return BackhaulReviewValidator(
        mutableListOf<BackhaulReviewRule>(
            BackhaulReviewFloorValueRangeRule(2.1, 1771.6),
            BackhaulReviewCodePrefixRule("REF"),
            BackhaulReviewAllowedCodeRule(mutableListOf<String>("strict")),
            BackhaulReviewRequiredTitleRule("backhaul review needs this field"),
        ),
    )
}
