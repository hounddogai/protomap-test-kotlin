package com.acme.transport.fleetops.tire.tireclaim

/** A rule a fallback tire rollup must pass. Check returns its problems, or none. */
interface FallbackTireRollupRule {
    fun check(item: FallbackTireRollup): MutableList<String>
}

data class FallbackTireRollupMarginRangeRule(var min: Double, var max: Double) : FallbackTireRollupRule {
    override fun check(item: FallbackTireRollup): MutableList<String> {
        val value = item.margin
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("margin $value is below $min")
        }
        if (value > max) {
            problems.add("margin $value is above $max")
        }
        return problems
    }
}

data class FallbackTireRollupTitleLengthRule(var maxLength: Int) : FallbackTireRollupRule {
    override fun check(item: FallbackTireRollup): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackTireRollupRequiredTitleRule(var message: String) : FallbackTireRollupRule {
    override fun check(item: FallbackTireRollup): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackTireRollupDepotLengthRule(var maxLength: Int) : FallbackTireRollupRule {
    override fun check(item: FallbackTireRollup): MutableList<String> {
        val size = item.depot.length
        if (size > maxLength) {
            return mutableListOf<String>("depot is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackTireRollupArchivedGuardRule(var limit: Int) : FallbackTireRollupRule {
    override fun check(item: FallbackTireRollup): MutableList<String> {
        if (item.archived && item.limit > limit) {
            return mutableListOf<String>("limit ${item.limit} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

data class FallbackTireRollupRequiredDepotRule(var message: String) : FallbackTireRollupRule {
    override fun check(item: FallbackTireRollup): MutableList<String> {
        if (item.depot.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback tire rollup. */
data class FallbackTireRollupValidator(var rules: MutableList<FallbackTireRollupRule>) {
    fun validate(item: FallbackTireRollup): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FallbackTireRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackTireRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackTireRollupValidator(): FallbackTireRollupValidator {
    return FallbackTireRollupValidator(
        mutableListOf<FallbackTireRollupRule>(
            FallbackTireRollupMarginRangeRule(2.7, 861.2),
            FallbackTireRollupTitleLengthRule(32),
            FallbackTireRollupRequiredTitleRule("fallback tire rollup needs this field"),
            FallbackTireRollupDepotLengthRule(32),
            FallbackTireRollupArchivedGuardRule(1000),
            FallbackTireRollupRequiredDepotRule("fallback tire rollup needs an entry"),
        ),
    )
}
