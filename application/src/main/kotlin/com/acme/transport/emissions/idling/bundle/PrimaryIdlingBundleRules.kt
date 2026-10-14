package com.acme.transport.emissions.idling.bundle

/** A rule a primary idling bundle must pass. Check returns its problems, or none. */
interface PrimaryIdlingBundleRule {
    fun check(item: PrimaryIdlingBundle): MutableList<String>
}

data class PrimaryIdlingBundleRequiredSummaryRule(var message: String) : PrimaryIdlingBundleRule {
    override fun check(item: PrimaryIdlingBundle): MutableList<String> {
        if (item.summary.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryIdlingBundleRequiredCategoryRule(var message: String) : PrimaryIdlingBundleRule {
    override fun check(item: PrimaryIdlingBundle): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryIdlingBundleSummaryLengthRule(var maxLength: Int) : PrimaryIdlingBundleRule {
    override fun check(item: PrimaryIdlingBundle): MutableList<String> {
        val size = item.summary.length
        if (size > maxLength) {
            return mutableListOf<String>("summary is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryIdlingBundleCategoryLengthRule(var maxLength: Int) : PrimaryIdlingBundleRule {
    override fun check(item: PrimaryIdlingBundle): MutableList<String> {
        val size = item.category.length
        if (size > maxLength) {
            return mutableListOf<String>("category is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary idling bundle. */
data class PrimaryIdlingBundleValidator(var rules: MutableList<PrimaryIdlingBundleRule>) {
    fun validate(item: PrimaryIdlingBundle): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PrimaryIdlingBundle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryIdlingBundle>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryIdlingBundleValidator(): PrimaryIdlingBundleValidator {
    return PrimaryIdlingBundleValidator(
        mutableListOf<PrimaryIdlingBundleRule>(
            PrimaryIdlingBundleRequiredSummaryRule("primary idling bundle needs an entry"),
            PrimaryIdlingBundleRequiredCategoryRule("primary idling bundle needs this field"),
            PrimaryIdlingBundleSummaryLengthRule(16),
            PrimaryIdlingBundleCategoryLengthRule(86400),
        ),
    )
}
