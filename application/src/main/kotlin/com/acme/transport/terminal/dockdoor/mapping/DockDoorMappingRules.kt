package com.acme.transport.terminal.dockdoor.mapping

import com.acme.transport.kit.text.normalize

/** A rule a dock door mapping must pass. Check returns its problems, or none. */
interface DockDoorMappingRule {
    fun check(item: DockDoorMapping): MutableList<String>
}

data class DockDoorMappingCodePrefixRule(var prefix: String) : DockDoorMappingRule {
    override fun check(item: DockDoorMapping): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DockDoorMappingTitleLengthRule(var maxLength: Int) : DockDoorMappingRule {
    override fun check(item: DockDoorMapping): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DockDoorMappingAllowedSummaryRule(var allowed: MutableList<String>) : DockDoorMappingRule {
    override fun check(item: DockDoorMapping): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.summary))) {
            return mutableListOf<String>("summary ${item.summary} is stale")
        }
        return mutableListOf<String>()
    }
}

data class DockDoorMappingRequiredTitleRule(var message: String) : DockDoorMappingRule {
    override fun check(item: DockDoorMapping): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DockDoorMappingRequiredSegmentRule(var message: String) : DockDoorMappingRule {
    override fun check(item: DockDoorMapping): MutableList<String> {
        if (item.segment.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a dock door mapping. */
data class DockDoorMappingValidator(var rules: MutableList<DockDoorMappingRule>) {
    fun validate(item: DockDoorMapping): MutableList<String> {
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

    fun requireValid(item: DockDoorMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DockDoorMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultDockDoorMappingValidator(): DockDoorMappingValidator {
    return DockDoorMappingValidator(
        mutableListOf<DockDoorMappingRule>(
            DockDoorMappingCodePrefixRule("ACM"),
            DockDoorMappingTitleLengthRule(64),
            DockDoorMappingAllowedSummaryRule(mutableListOf<String>()),
            DockDoorMappingRequiredTitleRule("dock door mapping needs an entry"),
            DockDoorMappingRequiredSegmentRule("dock door mapping needs this field"),
        ),
    )
}
