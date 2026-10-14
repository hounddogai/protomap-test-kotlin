package com.acme.transport.terminal.sortation.draft

/** A rule a tiered sortation digest must pass. Check returns its problems, or none. */
interface TieredSortationDigestRule {
    fun check(item: TieredSortationDigest): MutableList<String>
}

data class TieredSortationDigestTitleLengthRule(var maxLength: Int) : TieredSortationDigestRule {
    override fun check(item: TieredSortationDigest): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredSortationDigestRequiredExternalRefRule(var message: String) : TieredSortationDigestRule {
    override fun check(item: TieredSortationDigest): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredSortationDigestRequiredTitleRule(var message: String) : TieredSortationDigestRule {
    override fun check(item: TieredSortationDigest): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredSortationDigestRetriesRangeRule(var min: Int, var max: Int) : TieredSortationDigestRule {
    override fun check(item: TieredSortationDigest): MutableList<String> {
        val value = item.retries
        if (value < min || value > max) {
            return mutableListOf<String>("retries $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered sortation digest. */
data class TieredSortationDigestValidator(var rules: MutableList<TieredSortationDigestRule>) {
    fun validate(item: TieredSortationDigest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TieredSortationDigest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredSortationDigest>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredSortationDigestValidator(): TieredSortationDigestValidator {
    return TieredSortationDigestValidator(
        mutableListOf<TieredSortationDigestRule>(
            TieredSortationDigestTitleLengthRule(120),
            TieredSortationDigestRequiredExternalRefRule("tiered sortation digest needs this field"),
            TieredSortationDigestRequiredTitleRule("tiered sortation digest needs this field"),
            TieredSortationDigestRetriesRangeRule(3600, 86400),
        ),
    )
}
