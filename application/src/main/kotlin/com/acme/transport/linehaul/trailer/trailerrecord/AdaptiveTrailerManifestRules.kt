package com.acme.transport.linehaul.trailer.trailerrecord

/** A rule a adaptive trailer manifest must pass. Check returns its problems, or none. */
interface AdaptiveTrailerManifestRule {
    fun check(item: AdaptiveTrailerManifest): MutableList<String>
}

data class AdaptiveTrailerManifestLimitRangeRule(var min: Int, var max: Int) : AdaptiveTrailerManifestRule {
    override fun check(item: AdaptiveTrailerManifest): MutableList<String> {
        val value = item.limit
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("limit $value is below $min")
        }
        if (value > max) {
            problems.add("limit $value is above $max")
        }
        return problems
    }
}

data class AdaptiveTrailerManifestTitleLengthRule(var maxLength: Int) : AdaptiveTrailerManifestRule {
    override fun check(item: AdaptiveTrailerManifest): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveTrailerManifestRequiredCodeRule(var message: String) : AdaptiveTrailerManifestRule {
    override fun check(item: AdaptiveTrailerManifest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveTrailerManifestCodePrefixRule(var prefix: String) : AdaptiveTrailerManifestRule {
    override fun check(item: AdaptiveTrailerManifest): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveTrailerManifestTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : AdaptiveTrailerManifestRule {
    override fun check(item: AdaptiveTrailerManifest): MutableList<String> {
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

/** Runs every rule of a adaptive trailer manifest. */
data class AdaptiveTrailerManifestValidator(var rules: MutableList<AdaptiveTrailerManifestRule>) {
    fun validate(item: AdaptiveTrailerManifest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AdaptiveTrailerManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveTrailerManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveTrailerManifestValidator(): AdaptiveTrailerManifestValidator {
    return AdaptiveTrailerManifestValidator(
        mutableListOf<AdaptiveTrailerManifestRule>(
            AdaptiveTrailerManifestLimitRangeRule(80, 120),
            AdaptiveTrailerManifestTitleLengthRule(80),
            AdaptiveTrailerManifestRequiredCodeRule("adaptive trailer manifest needs an entry"),
            AdaptiveTrailerManifestCodePrefixRule("Q"),
            AdaptiveTrailerManifestTagsLimitRule(255, mutableListOf<String>("rolling")),
        ),
    )
}
