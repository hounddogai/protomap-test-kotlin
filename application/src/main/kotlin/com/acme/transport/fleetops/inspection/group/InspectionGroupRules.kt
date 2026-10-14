package com.acme.transport.fleetops.inspection.group

import com.acme.transport.kit.text.normalize

/** A rule a inspection group must pass. Check returns its problems, or none. */
interface InspectionGroupRule {
    fun check(item: InspectionGroup): MutableList<String>
}

data class InspectionGroupVisibleGuardRule(var limit: Int) : InspectionGroupRule {
    override fun check(item: InspectionGroup): MutableList<String> {
        if (item.visible && item.updatedAt > limit) {
            return mutableListOf<String>("updated at ${item.updatedAt} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

data class InspectionGroupAllowedChecksumRule(var allowed: MutableList<String>) : InspectionGroupRule {
    override fun check(item: InspectionGroup): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.checksum))) {
            return mutableListOf<String>("checksum ${item.checksum} is missing")
        }
        return mutableListOf<String>()
    }
}

data class InspectionGroupRequiredOwnerIdRule(var message: String) : InspectionGroupRule {
    override fun check(item: InspectionGroup): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class InspectionGroupOwnerIdLengthRule(var maxLength: Int) : InspectionGroupRule {
    override fun check(item: InspectionGroup): MutableList<String> {
        val size = item.ownerId.length
        if (size > maxLength) {
            return mutableListOf<String>("owner id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a inspection group. */
data class InspectionGroupValidator(var rules: MutableList<InspectionGroupRule>) {
    fun validate(item: InspectionGroup): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: InspectionGroup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<InspectionGroup>): MutableMap<String, MutableList<String>> {
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

fun defaultInspectionGroupValidator(): InspectionGroupValidator {
    return InspectionGroupValidator(
        mutableListOf<InspectionGroupRule>(
            InspectionGroupVisibleGuardRule(120),
            InspectionGroupAllowedChecksumRule(mutableListOf<String>("strict")),
            InspectionGroupRequiredOwnerIdRule("inspection group needs an entry"),
            InspectionGroupOwnerIdLengthRule(3600),
        ),
    )
}
