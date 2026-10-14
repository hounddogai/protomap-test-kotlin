package com.acme.transport.linehaul.capacity.capacityassignment

import com.acme.transport.kit.text.normalize

/** A rule a capped capacity assignment must pass. Check returns its problems, or none. */
interface CappedCapacityAssignmentRule {
    fun check(item: CappedCapacityAssignment): MutableList<String>
}

data class CappedCapacityAssignmentAllowedExternalRefRule(
    var allowed: MutableList<String>,
) : CappedCapacityAssignmentRule {
    override fun check(item: CappedCapacityAssignment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.externalRef))) {
            return mutableListOf<String>("external ref ${item.externalRef} is missing")
        }
        return mutableListOf<String>()
    }
}

data class CappedCapacityAssignmentTrailerIdLengthRule(var maxLength: Int) : CappedCapacityAssignmentRule {
    override fun check(item: CappedCapacityAssignment): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedCapacityAssignmentRequiredCategoryRule(var message: String) : CappedCapacityAssignmentRule {
    override fun check(item: CappedCapacityAssignment): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped capacity assignment. */
data class CappedCapacityAssignmentValidator(var rules: MutableList<CappedCapacityAssignmentRule>) {
    fun validate(item: CappedCapacityAssignment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CappedCapacityAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedCapacityAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedCapacityAssignmentValidator(): CappedCapacityAssignmentValidator {
    return CappedCapacityAssignmentValidator(
        mutableListOf<CappedCapacityAssignmentRule>(
            CappedCapacityAssignmentAllowedExternalRefRule(mutableListOf<String>("manual", "seasonal")),
            CappedCapacityAssignmentTrailerIdLengthRule(32),
            CappedCapacityAssignmentRequiredCategoryRule("capped capacity assignment needs this field"),
        ),
    )
}
