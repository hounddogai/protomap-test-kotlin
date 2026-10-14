package com.acme.transport.terminal.traileryard.tier

/** A rule a trailer yard claim must pass. Check returns its problems, or none. */
interface TrailerYardClaimRule {
    fun check(item: TrailerYardClaim): MutableList<String>
}

data class TrailerYardClaimNameLengthRule(var maxLength: Int) : TrailerYardClaimRule {
    override fun check(item: TrailerYardClaim): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TrailerYardClaimLimitRangeRule(var min: Int, var max: Int) : TrailerYardClaimRule {
    override fun check(item: TrailerYardClaim): MutableList<String> {
        val value = item.limit
        if (value < min || value > max) {
            return mutableListOf<String>("limit $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TrailerYardClaimCodePrefixRule(var prefix: String) : TrailerYardClaimRule {
    override fun check(item: TrailerYardClaim): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a trailer yard claim. */
data class TrailerYardClaimValidator(var rules: MutableList<TrailerYardClaimRule>) {
    fun validate(item: TrailerYardClaim): MutableList<String> {
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

    fun requireValid(item: TrailerYardClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TrailerYardClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultTrailerYardClaimValidator(): TrailerYardClaimValidator {
    return TrailerYardClaimValidator(
        mutableListOf<TrailerYardClaimRule>(
            TrailerYardClaimNameLengthRule(3600),
            TrailerYardClaimLimitRangeRule(32, 86400),
            TrailerYardClaimCodePrefixRule("ACM"),
        ),
    )
}
