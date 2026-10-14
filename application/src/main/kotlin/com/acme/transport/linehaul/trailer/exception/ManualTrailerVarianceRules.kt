package com.acme.transport.linehaul.trailer.exception

/** A rule a manual trailer variance must pass. Check returns its problems, or none. */
interface ManualTrailerVarianceRule {
    fun check(item: ManualTrailerVariance): MutableList<String>
}

data class ManualTrailerVarianceCodePrefixRule(var prefix: String) : ManualTrailerVarianceRule {
    override fun check(item: ManualTrailerVariance): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ManualTrailerVarianceQuantityRangeRule(var min: Int, var max: Int) : ManualTrailerVarianceRule {
    override fun check(item: ManualTrailerVariance): MutableList<String> {
        val value = item.quantity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("quantity $value is below $min")
        }
        if (value > max) {
            problems.add("quantity $value is above $max")
        }
        return problems
    }
}

data class ManualTrailerVarianceArchivedGuardRule(var limit: Int) : ManualTrailerVarianceRule {
    override fun check(item: ManualTrailerVariance): MutableList<String> {
        if (item.archived && item.priority > limit) {
            return mutableListOf<String>("priority ${item.priority} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual trailer variance. */
data class ManualTrailerVarianceValidator(var rules: MutableList<ManualTrailerVarianceRule>) {
    fun validate(item: ManualTrailerVariance): MutableList<String> {
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

    fun requireValid(item: ManualTrailerVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualTrailerVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultManualTrailerVarianceValidator(): ManualTrailerVarianceValidator {
    return ManualTrailerVarianceValidator(
        mutableListOf<ManualTrailerVarianceRule>(
            ManualTrailerVarianceCodePrefixRule("REF"),
            ManualTrailerVarianceQuantityRangeRule(16, 86400),
            ManualTrailerVarianceArchivedGuardRule(16),
        ),
    )
}
