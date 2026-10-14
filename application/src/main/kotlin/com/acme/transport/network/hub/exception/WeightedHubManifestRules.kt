package com.acme.transport.network.hub.exception

/** A rule a weighted hub manifest must pass. Check returns its problems, or none. */
interface WeightedHubManifestRule {
    fun check(item: WeightedHubManifest): MutableList<String>
}

data class WeightedHubManifestRequiredCurrencyRule(var message: String) : WeightedHubManifestRule {
    override fun check(item: WeightedHubManifest): MutableList<String> {
        if (item.currency.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedHubManifestRequiredCodeRule(var message: String) : WeightedHubManifestRule {
    override fun check(item: WeightedHubManifest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedHubManifestEtaMinutesRangeRule(var min: Int, var max: Int) : WeightedHubManifestRule {
    override fun check(item: WeightedHubManifest): MutableList<String> {
        val value = item.etaMinutes
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("eta minutes $value is below $min")
        }
        if (value > max) {
            problems.add("eta minutes $value is above $max")
        }
        return problems
    }
}

data class WeightedHubManifestRequiredCountryRule(var message: String) : WeightedHubManifestRule {
    override fun check(item: WeightedHubManifest): MutableList<String> {
        if (item.country.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedHubManifestCurrencyLengthRule(var maxLength: Int) : WeightedHubManifestRule {
    override fun check(item: WeightedHubManifest): MutableList<String> {
        val size = item.currency.length
        if (size > maxLength) {
            return mutableListOf<String>("currency is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedHubManifestTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : WeightedHubManifestRule {
    override fun check(item: WeightedHubManifest): MutableList<String> {
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

/** Runs every rule of a weighted hub manifest. */
data class WeightedHubManifestValidator(var rules: MutableList<WeightedHubManifestRule>) {
    fun validate(item: WeightedHubManifest): MutableList<String> {
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

    fun requireValid(item: WeightedHubManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedHubManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedHubManifestValidator(): WeightedHubManifestValidator {
    return WeightedHubManifestValidator(
        mutableListOf<WeightedHubManifestRule>(
            WeightedHubManifestRequiredCurrencyRule("weighted hub manifest needs this field"),
            WeightedHubManifestRequiredCodeRule("weighted hub manifest needs this field"),
            WeightedHubManifestEtaMinutesRangeRule(32, 64),
            WeightedHubManifestRequiredCountryRule("weighted hub manifest needs an entry"),
            WeightedHubManifestCurrencyLengthRule(80),
            WeightedHubManifestTagsLimitRule(80, mutableListOf<String>()),
        ),
    )
}
