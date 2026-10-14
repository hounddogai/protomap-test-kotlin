package com.acme.transport.terminal.sortation.tier

/** A rule a sortation cycle must pass. Check returns its problems, or none. */
interface SortationCycleRule {
    fun check(item: SortationCycle): MutableList<String>
}

data class SortationCycleLabelLengthRule(var maxLength: Int) : SortationCycleRule {
    override fun check(item: SortationCycle): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SortationCycleRequiredTitleRule(var message: String) : SortationCycleRule {
    override fun check(item: SortationCycle): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SortationCycleRequiredLabelRule(var message: String) : SortationCycleRule {
    override fun check(item: SortationCycle): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a sortation cycle. */
data class SortationCycleValidator(var rules: MutableList<SortationCycleRule>) {
    fun validate(item: SortationCycle): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SortationCycle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SortationCycle>): MutableMap<String, MutableList<String>> {
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

fun defaultSortationCycleValidator(): SortationCycleValidator {
    return SortationCycleValidator(
        mutableListOf<SortationCycleRule>(
            SortationCycleLabelLengthRule(16),
            SortationCycleRequiredTitleRule("sortation cycle needs this field"),
            SortationCycleRequiredLabelRule("sortation cycle needs this field"),
        ),
    )
}
