package com.acme.transport.fleetops.inspection.claim

/** A rule a inspection claim must pass. Check returns its problems, or none. */
interface InspectionClaimRule {
    fun check(item: InspectionClaim): MutableList<String>
}

data class InspectionClaimCodePrefixRule(var prefix: String) : InspectionClaimRule {
    override fun check(item: InspectionClaim): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class InspectionClaimRequiredTitleRule(var message: String) : InspectionClaimRule {
    override fun check(item: InspectionClaim): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class InspectionClaimNotesLengthRule(var maxLength: Int) : InspectionClaimRule {
    override fun check(item: InspectionClaim): MutableList<String> {
        val size = item.notes.length
        if (size > maxLength) {
            return mutableListOf<String>("notes is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class InspectionClaimRequiredDriverIdRule(var message: String) : InspectionClaimRule {
    override fun check(item: InspectionClaim): MutableList<String> {
        if (item.driverId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class InspectionClaimDriverIdLengthRule(var maxLength: Int) : InspectionClaimRule {
    override fun check(item: InspectionClaim): MutableList<String> {
        val size = item.driverId.length
        if (size > maxLength) {
            return mutableListOf<String>("driver id is $size characters, more than $maxLength")
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
            InspectionClaimCodePrefixRule("OPS"),
            InspectionClaimRequiredTitleRule("inspection claim needs this field"),
            InspectionClaimNotesLengthRule(3600),
            InspectionClaimRequiredDriverIdRule("inspection claim needs this field"),
            InspectionClaimDriverIdLengthRule(64),
        ),
    )
}
