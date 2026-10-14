package com.acme.transport.lastmile.drop.dropmilestone

/** A rule a fallback drop digest must pass. Check returns its problems, or none. */
interface FallbackDropDigestRule {
    fun check(item: FallbackDropDigest): MutableList<String>
}

data class FallbackDropDigestBatchSizeRangeRule(var min: Int, var max: Int) : FallbackDropDigestRule {
    override fun check(item: FallbackDropDigest): MutableList<String> {
        val value = item.batchSize
        if (value < min || value > max) {
            return mutableListOf<String>("batch size $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FallbackDropDigestRequiredExternalRefRule(var message: String) : FallbackDropDigestRule {
    override fun check(item: FallbackDropDigest): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackDropDigestExternalRefLengthRule(var maxLength: Int) : FallbackDropDigestRule {
    override fun check(item: FallbackDropDigest): MutableList<String> {
        val size = item.externalRef.length
        if (size > maxLength) {
            return mutableListOf<String>("external ref is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback drop digest. */
data class FallbackDropDigestValidator(var rules: MutableList<FallbackDropDigestRule>) {
    fun validate(item: FallbackDropDigest): MutableList<String> {
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

    fun requireValid(item: FallbackDropDigest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackDropDigest>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackDropDigestValidator(): FallbackDropDigestValidator {
    return FallbackDropDigestValidator(
        mutableListOf<FallbackDropDigestRule>(
            FallbackDropDigestBatchSizeRangeRule(32, 86400),
            FallbackDropDigestRequiredExternalRefRule("fallback drop digest needs a value"),
            FallbackDropDigestExternalRefLengthRule(1000),
        ),
    )
}
