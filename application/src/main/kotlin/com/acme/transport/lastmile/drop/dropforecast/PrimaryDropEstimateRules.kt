package com.acme.transport.lastmile.drop.dropforecast

/** A rule a primary drop estimate must pass. Check returns its problems, or none. */
interface PrimaryDropEstimateRule {
    fun check(item: PrimaryDropEstimate): MutableList<String>
}

data class PrimaryDropEstimatePalletsRangeRule(var min: Int, var max: Int) : PrimaryDropEstimateRule {
    override fun check(item: PrimaryDropEstimate): MutableList<String> {
        val value = item.pallets
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("pallets $value is below $min")
        }
        if (value > max) {
            problems.add("pallets $value is above $max")
        }
        return problems
    }
}

data class PrimaryDropEstimateLocaleLengthRule(var maxLength: Int) : PrimaryDropEstimateRule {
    override fun check(item: PrimaryDropEstimate): MutableList<String> {
        val size = item.locale.length
        if (size > maxLength) {
            return mutableListOf<String>("locale is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryDropEstimateVersionRangeRule(var min: Int, var max: Int) : PrimaryDropEstimateRule {
    override fun check(item: PrimaryDropEstimate): MutableList<String> {
        val value = item.version
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("version $value is below $min")
        }
        if (value > max) {
            problems.add("version $value is above $max")
        }
        return problems
    }
}

data class PrimaryDropEstimateTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : PrimaryDropEstimateRule {
    override fun check(item: PrimaryDropEstimate): MutableList<String> {
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

data class PrimaryDropEstimateCodePrefixRule(var prefix: String) : PrimaryDropEstimateRule {
    override fun check(item: PrimaryDropEstimate): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary drop estimate. */
data class PrimaryDropEstimateValidator(var rules: MutableList<PrimaryDropEstimateRule>) {
    fun validate(item: PrimaryDropEstimate): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PrimaryDropEstimate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryDropEstimate>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryDropEstimateValidator(): PrimaryDropEstimateValidator {
    return PrimaryDropEstimateValidator(
        mutableListOf<PrimaryDropEstimateRule>(
            PrimaryDropEstimatePalletsRangeRule(255, 3600),
            PrimaryDropEstimateLocaleLengthRule(86400),
            PrimaryDropEstimateVersionRangeRule(255, 255),
            PrimaryDropEstimateTagsLimitRule(120, mutableListOf<String>("rolling", "scheduled", "primary")),
            PrimaryDropEstimateCodePrefixRule("X"),
        ),
    )
}
