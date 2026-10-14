package com.acme.transport.emissions.evcharge.mapping

/** A rule a ev charge summary must pass. Check returns its problems, or none. */
interface EvChargeSummaryRule {
    fun check(item: EvChargeSummary): MutableList<String>
}

data class EvChargeSummaryRequiredCategoryRule(var message: String) : EvChargeSummaryRule {
    override fun check(item: EvChargeSummary): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class EvChargeSummaryTrailerIdLengthRule(var maxLength: Int) : EvChargeSummaryRule {
    override fun check(item: EvChargeSummary): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class EvChargeSummaryCategoryLengthRule(var maxLength: Int) : EvChargeSummaryRule {
    override fun check(item: EvChargeSummary): MutableList<String> {
        val size = item.category.length
        if (size > maxLength) {
            return mutableListOf<String>("category is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class EvChargeSummaryRequiredTrailerIdRule(var message: String) : EvChargeSummaryRule {
    override fun check(item: EvChargeSummary): MutableList<String> {
        if (item.trailerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a ev charge summary. */
data class EvChargeSummaryValidator(var rules: MutableList<EvChargeSummaryRule>) {
    fun validate(item: EvChargeSummary): MutableList<String> {
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

    fun requireValid(item: EvChargeSummary) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<EvChargeSummary>): MutableMap<String, MutableList<String>> {
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

fun defaultEvChargeSummaryValidator(): EvChargeSummaryValidator {
    return EvChargeSummaryValidator(
        mutableListOf<EvChargeSummaryRule>(
            EvChargeSummaryRequiredCategoryRule("ev charge summary needs a value"),
            EvChargeSummaryTrailerIdLengthRule(64),
            EvChargeSummaryCategoryLengthRule(120),
            EvChargeSummaryRequiredTrailerIdRule("ev charge summary needs a value"),
        ),
    )
}
