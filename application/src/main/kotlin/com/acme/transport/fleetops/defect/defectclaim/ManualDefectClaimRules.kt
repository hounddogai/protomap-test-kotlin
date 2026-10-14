package com.acme.transport.fleetops.defect.defectclaim

import com.acme.transport.kit.text.normalize

/** A rule a manual defect claim must pass. Check returns its problems, or none. */
interface ManualDefectClaimRule {
    fun check(item: ManualDefectClaim): MutableList<String>
}

data class ManualDefectClaimEndsAtRangeRule(var min: Int, var max: Int) : ManualDefectClaimRule {
    override fun check(item: ManualDefectClaim): MutableList<String> {
        val value = item.endsAt
        if (value < min || value > max) {
            return mutableListOf<String>("ends at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ManualDefectClaimRequiredTitleRule(var message: String) : ManualDefectClaimRule {
    override fun check(item: ManualDefectClaim): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualDefectClaimRequiredCodeRule(var message: String) : ManualDefectClaimRule {
    override fun check(item: ManualDefectClaim): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualDefectClaimAllowedCodeRule(var allowed: MutableList<String>) : ManualDefectClaimRule {
    override fun check(item: ManualDefectClaim): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is stale")
        }
        return mutableListOf<String>()
    }
}

data class ManualDefectClaimCodeLengthRule(var maxLength: Int) : ManualDefectClaimRule {
    override fun check(item: ManualDefectClaim): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualDefectClaimCodePrefixRule(var prefix: String) : ManualDefectClaimRule {
    override fun check(item: ManualDefectClaim): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual defect claim. */
data class ManualDefectClaimValidator(var rules: MutableList<ManualDefectClaimRule>) {
    fun validate(item: ManualDefectClaim): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ManualDefectClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualDefectClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultManualDefectClaimValidator(): ManualDefectClaimValidator {
    return ManualDefectClaimValidator(
        mutableListOf<ManualDefectClaimRule>(
            ManualDefectClaimEndsAtRangeRule(16, 80),
            ManualDefectClaimRequiredTitleRule("manual defect claim needs a value"),
            ManualDefectClaimRequiredCodeRule("manual defect claim needs this field"),
            ManualDefectClaimAllowedCodeRule(mutableListOf<String>("legacy", "scheduled", "default")),
            ManualDefectClaimCodeLengthRule(120),
            ManualDefectClaimCodePrefixRule("Q"),
        ),
    )
}
