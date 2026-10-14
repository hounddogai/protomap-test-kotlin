package com.acme.transport.emissions.idling.window

/** A rule a adaptive idling digest must pass. Check returns its problems, or none. */
interface AdaptiveIdlingDigestRule {
    fun check(item: AdaptiveIdlingDigest): MutableList<String>
}

data class AdaptiveIdlingDigestRequiredCodeRule(var message: String) : AdaptiveIdlingDigestRule {
    override fun check(item: AdaptiveIdlingDigest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveIdlingDigestTitleLengthRule(var maxLength: Int) : AdaptiveIdlingDigestRule {
    override fun check(item: AdaptiveIdlingDigest): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveIdlingDigestCountryLengthRule(var maxLength: Int) : AdaptiveIdlingDigestRule {
    override fun check(item: AdaptiveIdlingDigest): MutableList<String> {
        val size = item.country.length
        if (size > maxLength) {
            return mutableListOf<String>("country is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveIdlingDigestCategoryLengthRule(var maxLength: Int) : AdaptiveIdlingDigestRule {
    override fun check(item: AdaptiveIdlingDigest): MutableList<String> {
        val size = item.category.length
        if (size > maxLength) {
            return mutableListOf<String>("category is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveIdlingDigestRequiredTitleRule(var message: String) : AdaptiveIdlingDigestRule {
    override fun check(item: AdaptiveIdlingDigest): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive idling digest. */
data class AdaptiveIdlingDigestValidator(var rules: MutableList<AdaptiveIdlingDigestRule>) {
    fun validate(item: AdaptiveIdlingDigest): MutableList<String> {
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

    fun requireValid(item: AdaptiveIdlingDigest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveIdlingDigest>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveIdlingDigestValidator(): AdaptiveIdlingDigestValidator {
    return AdaptiveIdlingDigestValidator(
        mutableListOf<AdaptiveIdlingDigestRule>(
            AdaptiveIdlingDigestRequiredCodeRule("adaptive idling digest needs an entry"),
            AdaptiveIdlingDigestTitleLengthRule(16),
            AdaptiveIdlingDigestCountryLengthRule(255),
            AdaptiveIdlingDigestCategoryLengthRule(64),
            AdaptiveIdlingDigestRequiredTitleRule("adaptive idling digest needs an entry"),
        ),
    )
}
