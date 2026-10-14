package com.acme.transport.linehaul.trailer.cycle

/** A rule a primary trailer claim must pass. Check returns its problems, or none. */
interface PrimaryTrailerClaimRule {
    fun check(item: PrimaryTrailerClaim): MutableList<String>
}

data class PrimaryTrailerClaimRequiredLabelRule(var message: String) : PrimaryTrailerClaimRule {
    override fun check(item: PrimaryTrailerClaim): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryTrailerClaimCodePrefixRule(var prefix: String) : PrimaryTrailerClaimRule {
    override fun check(item: PrimaryTrailerClaim): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryTrailerClaimMarginRangeRule(var min: Double, var max: Double) : PrimaryTrailerClaimRule {
    override fun check(item: PrimaryTrailerClaim): MutableList<String> {
        val value = item.margin
        if (value < min || value > max) {
            return mutableListOf<String>("margin $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryTrailerClaimRequiredSegmentRule(var message: String) : PrimaryTrailerClaimRule {
    override fun check(item: PrimaryTrailerClaim): MutableList<String> {
        if (item.segment.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary trailer claim. */
data class PrimaryTrailerClaimValidator(var rules: MutableList<PrimaryTrailerClaimRule>) {
    fun validate(item: PrimaryTrailerClaim): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PrimaryTrailerClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryTrailerClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryTrailerClaimValidator(): PrimaryTrailerClaimValidator {
    return PrimaryTrailerClaimValidator(
        mutableListOf<PrimaryTrailerClaimRule>(
            PrimaryTrailerClaimRequiredLabelRule("primary trailer claim needs a value"),
            PrimaryTrailerClaimCodePrefixRule("OPS"),
            PrimaryTrailerClaimMarginRangeRule(1.0, 1599.8),
            PrimaryTrailerClaimRequiredSegmentRule("primary trailer claim needs an entry"),
        ),
    )
}
