package com.acme.transport.terminal.sortation.bundle

/** A rule a sortation segment must pass. Check returns its problems, or none. */
interface SortationSegmentRule {
    fun check(item: SortationSegment): MutableList<String>
}

data class SortationSegmentRequiredCodeRule(var message: String) : SortationSegmentRule {
    override fun check(item: SortationSegment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SortationSegmentCodePrefixRule(var prefix: String) : SortationSegmentRule {
    override fun check(item: SortationSegment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class SortationSegmentTrailerIdLengthRule(var maxLength: Int) : SortationSegmentRule {
    override fun check(item: SortationSegment): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a sortation segment. */
data class SortationSegmentValidator(var rules: MutableList<SortationSegmentRule>) {
    fun validate(item: SortationSegment): MutableList<String> {
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

    fun requireValid(item: SortationSegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SortationSegment>): MutableMap<String, MutableList<String>> {
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

fun defaultSortationSegmentValidator(): SortationSegmentValidator {
    return SortationSegmentValidator(
        mutableListOf<SortationSegmentRule>(
            SortationSegmentRequiredCodeRule("sortation segment needs an entry"),
            SortationSegmentCodePrefixRule("ACM"),
            SortationSegmentTrailerIdLengthRule(32),
        ),
    )
}
