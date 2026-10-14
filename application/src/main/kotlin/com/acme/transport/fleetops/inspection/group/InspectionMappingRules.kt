package com.acme.transport.fleetops.inspection.group

/** A rule a inspection mapping must pass. Check returns its problems, or none. */
interface InspectionMappingRule {
    fun check(item: InspectionMapping): MutableList<String>
}

data class InspectionMappingRequiredCodeRule(var message: String) : InspectionMappingRule {
    override fun check(item: InspectionMapping): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class InspectionMappingNotesLengthRule(var maxLength: Int) : InspectionMappingRule {
    override fun check(item: InspectionMapping): MutableList<String> {
        val size = item.notes.length
        if (size > maxLength) {
            return mutableListOf<String>("notes is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class InspectionMappingApprovedGuardRule(var limit: Int) : InspectionMappingRule {
    override fun check(item: InspectionMapping): MutableList<String> {
        if (item.approved && item.priority > limit) {
            return mutableListOf<String>("priority ${item.priority} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a inspection mapping. */
data class InspectionMappingValidator(var rules: MutableList<InspectionMappingRule>) {
    fun validate(item: InspectionMapping): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: InspectionMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<InspectionMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultInspectionMappingValidator(): InspectionMappingValidator {
    return InspectionMappingValidator(
        mutableListOf<InspectionMappingRule>(
            InspectionMappingRequiredCodeRule("inspection mapping needs this field"),
            InspectionMappingNotesLengthRule(16),
            InspectionMappingApprovedGuardRule(255),
        ),
    )
}
