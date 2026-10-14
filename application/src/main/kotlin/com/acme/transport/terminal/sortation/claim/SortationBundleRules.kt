package com.acme.transport.terminal.sortation.claim

/** A rule a sortation bundle must pass. Check returns its problems, or none. */
interface SortationBundleRule {
    fun check(item: SortationBundle): MutableList<String>
}

data class SortationBundleReviewerLengthRule(var maxLength: Int) : SortationBundleRule {
    override fun check(item: SortationBundle): MutableList<String> {
        val size = item.reviewer.length
        if (size > maxLength) {
            return mutableListOf<String>("reviewer is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SortationBundleTitleLengthRule(var maxLength: Int) : SortationBundleRule {
    override fun check(item: SortationBundle): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SortationBundleRequiredExternalRefRule(var message: String) : SortationBundleRule {
    override fun check(item: SortationBundle): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a sortation bundle. */
data class SortationBundleValidator(var rules: MutableList<SortationBundleRule>) {
    fun validate(item: SortationBundle): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SortationBundle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SortationBundle>): MutableMap<String, MutableList<String>> {
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

fun defaultSortationBundleValidator(): SortationBundleValidator {
    return SortationBundleValidator(
        mutableListOf<SortationBundleRule>(
            SortationBundleReviewerLengthRule(32),
            SortationBundleTitleLengthRule(255),
            SortationBundleRequiredExternalRefRule("sortation bundle needs a value"),
        ),
    )
}
