package com.acme.transport.fleetops.defect.claim

/** A rule a defect claim must pass. Check returns its problems, or none. */
interface DefectClaimRule {
    fun check(item: DefectClaim): MutableList<String>
}

data class DefectClaimLocaleLengthRule(var maxLength: Int) : DefectClaimRule {
    override fun check(item: DefectClaim): MutableList<String> {
        val size = item.locale.length
        if (size > maxLength) {
            return mutableListOf<String>("locale is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefectClaimRequiredCodeRule(var message: String) : DefectClaimRule {
    override fun check(item: DefectClaim): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefectClaimCodePrefixRule(var prefix: String) : DefectClaimRule {
    override fun check(item: DefectClaim): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a defect claim. */
data class DefectClaimValidator(var rules: MutableList<DefectClaimRule>) {
    fun validate(item: DefectClaim): MutableList<String> {
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

    fun requireValid(item: DefectClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefectClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultDefectClaimValidator(): DefectClaimValidator {
    return DefectClaimValidator(
        mutableListOf<DefectClaimRule>(
            DefectClaimLocaleLengthRule(80),
            DefectClaimRequiredCodeRule("defect claim needs an entry"),
            DefectClaimCodePrefixRule("X"),
        ),
    )
}
