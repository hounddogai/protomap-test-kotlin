package com.acme.transport.customs.inspection.claim

/** A rule a inspection claim must pass. Check returns its problems, or none. */
interface InspectionClaimRule {
    fun check(item: InspectionClaim): MutableList<String>
}

data class InspectionClaimOwnerIdLengthRule(var maxLength: Int) : InspectionClaimRule {
    override fun check(item: InspectionClaim): MutableList<String> {
        val size = item.ownerId.length
        if (size > maxLength) {
            return mutableListOf<String>("owner id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class InspectionClaimStopCountRangeRule(var min: Int, var max: Int) : InspectionClaimRule {
    override fun check(item: InspectionClaim): MutableList<String> {
        val value = item.stopCount
        if (value < min || value > max) {
            return mutableListOf<String>("stop count $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class InspectionClaimRequiredOwnerIdRule(var message: String) : InspectionClaimRule {
    override fun check(item: InspectionClaim): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class InspectionClaimRequiredCodeRule(var message: String) : InspectionClaimRule {
    override fun check(item: InspectionClaim): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class InspectionClaimCodeLengthRule(var maxLength: Int) : InspectionClaimRule {
    override fun check(item: InspectionClaim): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a inspection claim. */
data class InspectionClaimValidator(var rules: MutableList<InspectionClaimRule>) {
    fun validate(item: InspectionClaim): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: InspectionClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<InspectionClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultInspectionClaimValidator(): InspectionClaimValidator {
    return InspectionClaimValidator(
        mutableListOf<InspectionClaimRule>(
            InspectionClaimOwnerIdLengthRule(86400),
            InspectionClaimStopCountRangeRule(1000, 86400),
            InspectionClaimRequiredOwnerIdRule("inspection claim needs this field"),
            InspectionClaimRequiredCodeRule("inspection claim needs this field"),
            InspectionClaimCodeLengthRule(255),
        ),
    )
}
