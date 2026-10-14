package com.acme.transport.terminal.sortation.recorditem

import com.acme.transport.kit.text.normalize

/** A rule a sortation assignment must pass. Check returns its problems, or none. */
interface SortationAssignmentRule {
    fun check(item: SortationAssignment): MutableList<String>
}

data class SortationAssignmentRequiredTitleRule(var message: String) : SortationAssignmentRule {
    override fun check(item: SortationAssignment): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SortationAssignmentRequiredCodeRule(var message: String) : SortationAssignmentRule {
    override fun check(item: SortationAssignment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SortationAssignmentAllowedTitleRule(var allowed: MutableList<String>) : SortationAssignmentRule {
    override fun check(item: SortationAssignment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class SortationAssignmentTitleLengthRule(var maxLength: Int) : SortationAssignmentRule {
    override fun check(item: SortationAssignment): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a sortation assignment. */
data class SortationAssignmentValidator(var rules: MutableList<SortationAssignmentRule>) {
    fun validate(item: SortationAssignment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SortationAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SortationAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultSortationAssignmentValidator(): SortationAssignmentValidator {
    return SortationAssignmentValidator(
        mutableListOf<SortationAssignmentRule>(
            SortationAssignmentRequiredTitleRule("sortation assignment needs a value"),
            SortationAssignmentRequiredCodeRule("sortation assignment needs this field"),
            SortationAssignmentAllowedTitleRule(mutableListOf<String>("primary")),
            SortationAssignmentTitleLengthRule(3600),
        ),
    )
}
