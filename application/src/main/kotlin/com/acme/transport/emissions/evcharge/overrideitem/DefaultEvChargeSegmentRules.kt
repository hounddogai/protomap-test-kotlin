package com.acme.transport.emissions.evcharge.overrideitem

/** A rule a default ev charge segment must pass. Check returns its problems, or none. */
interface DefaultEvChargeSegmentRule {
    fun check(item: DefaultEvChargeSegment): MutableList<String>
}

data class DefaultEvChargeSegmentLabelLengthRule(var maxLength: Int) : DefaultEvChargeSegmentRule {
    override fun check(item: DefaultEvChargeSegment): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultEvChargeSegmentCategoryLengthRule(var maxLength: Int) : DefaultEvChargeSegmentRule {
    override fun check(item: DefaultEvChargeSegment): MutableList<String> {
        val size = item.category.length
        if (size > maxLength) {
            return mutableListOf<String>("category is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultEvChargeSegmentRequiredCategoryRule(var message: String) : DefaultEvChargeSegmentRule {
    override fun check(item: DefaultEvChargeSegment): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default ev charge segment. */
data class DefaultEvChargeSegmentValidator(var rules: MutableList<DefaultEvChargeSegmentRule>) {
    fun validate(item: DefaultEvChargeSegment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefaultEvChargeSegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultEvChargeSegment>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultEvChargeSegmentValidator(): DefaultEvChargeSegmentValidator {
    return DefaultEvChargeSegmentValidator(
        mutableListOf<DefaultEvChargeSegmentRule>(
            DefaultEvChargeSegmentLabelLengthRule(120),
            DefaultEvChargeSegmentCategoryLengthRule(3600),
            DefaultEvChargeSegmentRequiredCategoryRule("default ev charge segment needs an entry"),
        ),
    )
}
