package com.acme.transport.terminal.sortation.sortationbaseline

/** A rule a regional sortation claim must pass. Check returns its problems, or none. */
interface RegionalSortationClaimRule {
    fun check(item: RegionalSortationClaim): MutableList<String>
}

data class RegionalSortationClaimRequiredNameRule(var message: String) : RegionalSortationClaimRule {
    override fun check(item: RegionalSortationClaim): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalSortationClaimRequiredCodeRule(var message: String) : RegionalSortationClaimRule {
    override fun check(item: RegionalSortationClaim): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalSortationClaimCodePrefixRule(var prefix: String) : RegionalSortationClaimRule {
    override fun check(item: RegionalSortationClaim): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RegionalSortationClaimGraceDaysRangeRule(var min: Int, var max: Int) : RegionalSortationClaimRule {
    override fun check(item: RegionalSortationClaim): MutableList<String> {
        val value = item.graceDays
        if (value < min || value > max) {
            return mutableListOf<String>("grace days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RegionalSortationClaimExpiresAtRangeRule(var min: Int, var max: Int) : RegionalSortationClaimRule {
    override fun check(item: RegionalSortationClaim): MutableList<String> {
        val value = item.expiresAt
        if (value < min || value > max) {
            return mutableListOf<String>("expires at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RegionalSortationClaimTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : RegionalSortationClaimRule {
    override fun check(item: RegionalSortationClaim): MutableList<String> {
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

/** Runs every rule of a regional sortation claim. */
data class RegionalSortationClaimValidator(var rules: MutableList<RegionalSortationClaimRule>) {
    fun validate(item: RegionalSortationClaim): MutableList<String> {
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

    fun requireValid(item: RegionalSortationClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalSortationClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalSortationClaimValidator(): RegionalSortationClaimValidator {
    return RegionalSortationClaimValidator(
        mutableListOf<RegionalSortationClaimRule>(
            RegionalSortationClaimRequiredNameRule("regional sortation claim needs an entry"),
            RegionalSortationClaimRequiredCodeRule("regional sortation claim needs an entry"),
            RegionalSortationClaimCodePrefixRule("X"),
            RegionalSortationClaimGraceDaysRangeRule(16, 86400),
            RegionalSortationClaimExpiresAtRangeRule(255, 1000),
            RegionalSortationClaimTagsLimitRule(255, mutableListOf<String>("rolling")),
        ),
    )
}
