package com.acme.transport.lastmile.redelivery.request

/** A rule a redelivery window must pass. Check returns its problems, or none. */
interface RedeliveryWindowRule {
    fun check(item: RedeliveryWindow): MutableList<String>
}

data class RedeliveryWindowTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : RedeliveryWindowRule {
    override fun check(item: RedeliveryWindow): MutableList<String> {
        val problems = mutableListOf<String>()
        if (item.tags.size > maxCount) {
            problems.add("has ${item.tags.size} tags, at most $maxCount allowed")
        }
        for (tag in item.tags) {
            if (banned.contains(tag)) {
                problems.add("tag $tag is not allowed")
            }
        }
        return problems
    }
}

data class RedeliveryWindowCodePrefixRule(var prefix: String) : RedeliveryWindowRule {
    override fun check(item: RedeliveryWindow): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RedeliveryWindowDistanceKmRangeRule(var min: Double, var max: Double) : RedeliveryWindowRule {
    override fun check(item: RedeliveryWindow): MutableList<String> {
        val value = item.distanceKm
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("distance km $value is below $min")
        }
        if (value > max) {
            problems.add("distance km $value is above $max")
        }
        return problems
    }
}

data class RedeliveryWindowLimitRangeRule(var min: Int, var max: Int) : RedeliveryWindowRule {
    override fun check(item: RedeliveryWindow): MutableList<String> {
        val value = item.limit
        if (value < min || value > max) {
            return mutableListOf<String>("limit $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RedeliveryWindowRequiredCodeRule(var message: String) : RedeliveryWindowRule {
    override fun check(item: RedeliveryWindow): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RedeliveryWindowRequiredCurrencyRule(var message: String) : RedeliveryWindowRule {
    override fun check(item: RedeliveryWindow): MutableList<String> {
        if (item.currency.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a redelivery window. */
data class RedeliveryWindowValidator(var rules: MutableList<RedeliveryWindowRule>) {
    fun validate(item: RedeliveryWindow): MutableList<String> {
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

    fun requireValid(item: RedeliveryWindow) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RedeliveryWindow>): MutableMap<String, MutableList<String>> {
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

fun defaultRedeliveryWindowValidator(): RedeliveryWindowValidator {
    return RedeliveryWindowValidator(
        mutableListOf<RedeliveryWindowRule>(
            RedeliveryWindowTagsLimitRule(86400, mutableListOf<String>()),
            RedeliveryWindowCodePrefixRule("OPS"),
            RedeliveryWindowDistanceKmRangeRule(4.5, 3680.8),
            RedeliveryWindowLimitRangeRule(64, 80),
            RedeliveryWindowRequiredCodeRule("redelivery window needs an entry"),
            RedeliveryWindowRequiredCurrencyRule("redelivery window needs an entry"),
        ),
    )
}
