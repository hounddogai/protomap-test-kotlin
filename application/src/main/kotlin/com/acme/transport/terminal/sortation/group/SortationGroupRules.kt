package com.acme.transport.terminal.sortation.group

import com.acme.transport.kit.text.normalize

/** A rule a sortation group must pass. Check returns its problems, or none. */
interface SortationGroupRule {
    fun check(item: SortationGroup): MutableList<String>
}

data class SortationGroupRequiredSegmentRule(var message: String) : SortationGroupRule {
    override fun check(item: SortationGroup): MutableList<String> {
        if (item.segment.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SortationGroupRequiredUnitRule(var message: String) : SortationGroupRule {
    override fun check(item: SortationGroup): MutableList<String> {
        if (item.unit.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SortationGroupAllowedCodeRule(var allowed: MutableList<String>) : SortationGroupRule {
    override fun check(item: SortationGroup): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class SortationGroupRequiredCodeRule(var message: String) : SortationGroupRule {
    override fun check(item: SortationGroup): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SortationGroupCapacityRangeRule(var min: Int, var max: Int) : SortationGroupRule {
    override fun check(item: SortationGroup): MutableList<String> {
        val value = item.capacity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("capacity $value is below $min")
        }
        if (value > max) {
            problems.add("capacity $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a sortation group. */
data class SortationGroupValidator(var rules: MutableList<SortationGroupRule>) {
    fun validate(item: SortationGroup): MutableList<String> {
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

    fun requireValid(item: SortationGroup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SortationGroup>): MutableMap<String, MutableList<String>> {
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

fun defaultSortationGroupValidator(): SortationGroupValidator {
    return SortationGroupValidator(
        mutableListOf<SortationGroupRule>(
            SortationGroupRequiredSegmentRule("sortation group needs this field"),
            SortationGroupRequiredUnitRule("sortation group needs this field"),
            SortationGroupAllowedCodeRule(mutableListOf<String>()),
            SortationGroupRequiredCodeRule("sortation group needs an entry"),
            SortationGroupCapacityRangeRule(16, 64),
        ),
    )
}
