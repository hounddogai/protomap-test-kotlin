package com.acme.transport.emissions.emissionfactor.entry

/** A rule a pending emission factor claim must pass. Check returns its problems, or none. */
interface PendingEmissionFactorClaimRule {
    fun check(item: PendingEmissionFactorClaim): MutableList<String>
}

data class PendingEmissionFactorClaimLabelLengthRule(var maxLength: Int) : PendingEmissionFactorClaimRule {
    override fun check(item: PendingEmissionFactorClaim): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingEmissionFactorClaimTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : PendingEmissionFactorClaimRule {
    override fun check(item: PendingEmissionFactorClaim): MutableList<String> {
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

data class PendingEmissionFactorClaimStopCountRangeRule(
    var min: Int,
    var max: Int,
) : PendingEmissionFactorClaimRule {
    override fun check(item: PendingEmissionFactorClaim): MutableList<String> {
        val value = item.stopCount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("stop count $value is below $min")
        }
        if (value > max) {
            problems.add("stop count $value is above $max")
        }
        return problems
    }
}

data class PendingEmissionFactorClaimCodePrefixRule(var prefix: String) : PendingEmissionFactorClaimRule {
    override fun check(item: PendingEmissionFactorClaim): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending emission factor claim. */
data class PendingEmissionFactorClaimValidator(var rules: MutableList<PendingEmissionFactorClaimRule>) {
    fun validate(item: PendingEmissionFactorClaim): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PendingEmissionFactorClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingEmissionFactorClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingEmissionFactorClaimValidator(): PendingEmissionFactorClaimValidator {
    return PendingEmissionFactorClaimValidator(
        mutableListOf<PendingEmissionFactorClaimRule>(
            PendingEmissionFactorClaimLabelLengthRule(64),
            PendingEmissionFactorClaimTagsLimitRule(120, mutableListOf<String>("primary", "seasonal")),
            PendingEmissionFactorClaimStopCountRangeRule(255, 1000),
            PendingEmissionFactorClaimCodePrefixRule("REF"),
        ),
    )
}
