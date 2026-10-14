package com.acme.transport.fleetops.defect.allocation

import com.acme.transport.kit.text.normalize

/** A rule a defect allocation must pass. Check returns its problems, or none. */
interface DefectAllocationRule {
    fun check(item: DefectAllocation): MutableList<String>
}

data class DefectAllocationChannelLengthRule(var maxLength: Int) : DefectAllocationRule {
    override fun check(item: DefectAllocation): MutableList<String> {
        val size = item.channel.length
        if (size > maxLength) {
            return mutableListOf<String>("channel is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefectAllocationTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : DefectAllocationRule {
    override fun check(item: DefectAllocation): MutableList<String> {
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

data class DefectAllocationAllowedLabelRule(var allowed: MutableList<String>) : DefectAllocationRule {
    override fun check(item: DefectAllocation): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class DefectAllocationCodeLengthRule(var maxLength: Int) : DefectAllocationRule {
    override fun check(item: DefectAllocation): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a defect allocation. */
data class DefectAllocationValidator(var rules: MutableList<DefectAllocationRule>) {
    fun validate(item: DefectAllocation): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefectAllocation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefectAllocation>): MutableMap<String, MutableList<String>> {
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

fun defaultDefectAllocationValidator(): DefectAllocationValidator {
    return DefectAllocationValidator(
        mutableListOf<DefectAllocationRule>(
            DefectAllocationChannelLengthRule(120),
            DefectAllocationTagsLimitRule(86400, mutableListOf<String>("seasonal")),
            DefectAllocationAllowedLabelRule(mutableListOf<String>("default")),
            DefectAllocationCodeLengthRule(255),
        ),
    )
}
