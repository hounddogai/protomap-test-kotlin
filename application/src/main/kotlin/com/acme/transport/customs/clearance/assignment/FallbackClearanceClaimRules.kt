package com.acme.transport.customs.clearance.assignment

import com.acme.transport.kit.text.normalize

/** A rule a fallback clearance claim must pass. Check returns its problems, or none. */
interface FallbackClearanceClaimRule {
    fun check(item: FallbackClearanceClaim): MutableList<String>
}

data class FallbackClearanceClaimStopCountRangeRule(var min: Int, var max: Int) : FallbackClearanceClaimRule {
    override fun check(item: FallbackClearanceClaim): MutableList<String> {
        val value = item.stopCount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("stop count $value is below $min")
        }
        if (value > max) {
            problems.add("stop count $value is above $max")
        }
        return problems
    }
}

data class FallbackClearanceClaimAllowedShiftCodeRule(
    var allowed: MutableList<String>,
) : FallbackClearanceClaimRule {
    override fun check(item: FallbackClearanceClaim): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.shiftCode))) {
            return mutableListOf<String>("shift code ${item.shiftCode} is stale")
        }
        return mutableListOf<String>()
    }
}

data class FallbackClearanceClaimRequiredLabelRule(var message: String) : FallbackClearanceClaimRule {
    override fun check(item: FallbackClearanceClaim): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackClearanceClaimRequiredShiftCodeRule(var message: String) : FallbackClearanceClaimRule {
    override fun check(item: FallbackClearanceClaim): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackClearanceClaimEtaMinutesRangeRule(var min: Int, var max: Int) : FallbackClearanceClaimRule {
    override fun check(item: FallbackClearanceClaim): MutableList<String> {
        val value = item.etaMinutes
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("eta minutes $value is below $min")
        }
        if (value > max) {
            problems.add("eta minutes $value is above $max")
        }
        return problems
    }
}

data class FallbackClearanceClaimRequiredCodeRule(var message: String) : FallbackClearanceClaimRule {
    override fun check(item: FallbackClearanceClaim): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback clearance claim. */
data class FallbackClearanceClaimValidator(var rules: MutableList<FallbackClearanceClaimRule>) {
    fun validate(item: FallbackClearanceClaim): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FallbackClearanceClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackClearanceClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackClearanceClaimValidator(): FallbackClearanceClaimValidator {
    return FallbackClearanceClaimValidator(
        mutableListOf<FallbackClearanceClaimRule>(
            FallbackClearanceClaimStopCountRangeRule(80, 120),
            FallbackClearanceClaimAllowedShiftCodeRule(mutableListOf<String>()),
            FallbackClearanceClaimRequiredLabelRule("fallback clearance claim needs an entry"),
            FallbackClearanceClaimRequiredShiftCodeRule("fallback clearance claim needs this field"),
            FallbackClearanceClaimEtaMinutesRangeRule(32, 64),
            FallbackClearanceClaimRequiredCodeRule("fallback clearance claim needs this field"),
        ),
    )
}
