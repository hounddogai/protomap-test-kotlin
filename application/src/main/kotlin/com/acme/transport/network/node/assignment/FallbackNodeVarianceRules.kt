package com.acme.transport.network.node.assignment

/** A rule a fallback node variance must pass. Check returns its problems, or none. */
interface FallbackNodeVarianceRule {
    fun check(item: FallbackNodeVariance): MutableList<String>
}

data class FallbackNodeVarianceRequiredLabelRule(var message: String) : FallbackNodeVarianceRule {
    override fun check(item: FallbackNodeVariance): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackNodeVarianceApprovedGuardRule(var limit: Int) : FallbackNodeVarianceRule {
    override fun check(item: FallbackNodeVariance): MutableList<String> {
        if (item.approved && item.startsAt > limit) {
            return mutableListOf<String>("starts at ${item.startsAt} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

data class FallbackNodeVarianceCodePrefixRule(var prefix: String) : FallbackNodeVarianceRule {
    override fun check(item: FallbackNodeVariance): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class FallbackNodeVarianceLabelLengthRule(var maxLength: Int) : FallbackNodeVarianceRule {
    override fun check(item: FallbackNodeVariance): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback node variance. */
data class FallbackNodeVarianceValidator(var rules: MutableList<FallbackNodeVarianceRule>) {
    fun validate(item: FallbackNodeVariance): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FallbackNodeVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackNodeVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackNodeVarianceValidator(): FallbackNodeVarianceValidator {
    return FallbackNodeVarianceValidator(
        mutableListOf<FallbackNodeVarianceRule>(
            FallbackNodeVarianceRequiredLabelRule("fallback node variance needs this field"),
            FallbackNodeVarianceApprovedGuardRule(32),
            FallbackNodeVarianceCodePrefixRule("X"),
            FallbackNodeVarianceLabelLengthRule(64),
        ),
    )
}
