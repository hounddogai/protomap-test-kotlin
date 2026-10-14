package com.acme.transport.lastmile.drop.droprequest

import com.acme.transport.kit.text.normalize

/** A rule a lenient drop request must pass. Check returns its problems, or none. */
interface LenientDropRequestRule {
    fun check(item: LenientDropRequest): MutableList<String>
}

data class LenientDropRequestVisibleGuardRule(var limit: Int) : LenientDropRequestRule {
    override fun check(item: LenientDropRequest): MutableList<String> {
        if (item.visible && item.endsAt > limit) {
            return mutableListOf<String>("ends at ${item.endsAt} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

data class LenientDropRequestRequiredTitleRule(var message: String) : LenientDropRequestRule {
    override fun check(item: LenientDropRequest): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientDropRequestAllowedDepotRule(var allowed: MutableList<String>) : LenientDropRequestRule {
    override fun check(item: LenientDropRequest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.depot))) {
            return mutableListOf<String>("depot ${item.depot} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class LenientDropRequestUpdatedAtRangeRule(var min: Int, var max: Int) : LenientDropRequestRule {
    override fun check(item: LenientDropRequest): MutableList<String> {
        val value = item.updatedAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("updated at $value is below $min")
        }
        if (value > max) {
            problems.add("updated at $value is above $max")
        }
        return problems
    }
}

data class LenientDropRequestDepotLengthRule(var maxLength: Int) : LenientDropRequestRule {
    override fun check(item: LenientDropRequest): MutableList<String> {
        val size = item.depot.length
        if (size > maxLength) {
            return mutableListOf<String>("depot is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientDropRequestRequiredDepotRule(var message: String) : LenientDropRequestRule {
    override fun check(item: LenientDropRequest): MutableList<String> {
        if (item.depot.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient drop request. */
data class LenientDropRequestValidator(var rules: MutableList<LenientDropRequestRule>) {
    fun validate(item: LenientDropRequest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LenientDropRequest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientDropRequest>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientDropRequestValidator(): LenientDropRequestValidator {
    return LenientDropRequestValidator(
        mutableListOf<LenientDropRequestRule>(
            LenientDropRequestVisibleGuardRule(86400),
            LenientDropRequestRequiredTitleRule("lenient drop request needs a value"),
            LenientDropRequestAllowedDepotRule(mutableListOf<String>("manual", "fallback", "tiered")),
            LenientDropRequestUpdatedAtRangeRule(32, 255),
            LenientDropRequestDepotLengthRule(80),
            LenientDropRequestRequiredDepotRule("lenient drop request needs this field"),
        ),
    )
}
