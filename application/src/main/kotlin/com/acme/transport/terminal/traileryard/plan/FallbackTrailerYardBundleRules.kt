package com.acme.transport.terminal.traileryard.plan

/** A rule a fallback trailer yard bundle must pass. Check returns its problems, or none. */
interface FallbackTrailerYardBundleRule {
    fun check(item: FallbackTrailerYardBundle): MutableList<String>
}

data class FallbackTrailerYardBundleRequiredSummaryRule(var message: String) : FallbackTrailerYardBundleRule {
    override fun check(item: FallbackTrailerYardBundle): MutableList<String> {
        if (item.summary.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackTrailerYardBundleCodePrefixRule(var prefix: String) : FallbackTrailerYardBundleRule {
    override fun check(item: FallbackTrailerYardBundle): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class FallbackTrailerYardBundleVisibleGuardRule(var limit: Int) : FallbackTrailerYardBundleRule {
    override fun check(item: FallbackTrailerYardBundle): MutableList<String> {
        if (item.visible && item.stopCount > limit) {
            return mutableListOf<String>("stop count ${item.stopCount} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback trailer yard bundle. */
data class FallbackTrailerYardBundleValidator(var rules: MutableList<FallbackTrailerYardBundleRule>) {
    fun validate(item: FallbackTrailerYardBundle): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FallbackTrailerYardBundle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackTrailerYardBundle>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackTrailerYardBundleValidator(): FallbackTrailerYardBundleValidator {
    return FallbackTrailerYardBundleValidator(
        mutableListOf<FallbackTrailerYardBundleRule>(
            FallbackTrailerYardBundleRequiredSummaryRule("fallback trailer yard bundle needs a value"),
            FallbackTrailerYardBundleCodePrefixRule("X"),
            FallbackTrailerYardBundleVisibleGuardRule(80),
        ),
    )
}
