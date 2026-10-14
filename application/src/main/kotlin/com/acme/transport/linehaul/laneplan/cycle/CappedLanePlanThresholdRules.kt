package com.acme.transport.linehaul.laneplan.cycle

import com.acme.transport.kit.text.normalize

/** A rule a capped lane plan threshold must pass. Check returns its problems, or none. */
interface CappedLanePlanThresholdRule {
    fun check(item: CappedLanePlanThreshold): MutableList<String>
}

data class CappedLanePlanThresholdRequiredCodeRule(var message: String) : CappedLanePlanThresholdRule {
    override fun check(item: CappedLanePlanThreshold): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedLanePlanThresholdShiftCodeLengthRule(var maxLength: Int) : CappedLanePlanThresholdRule {
    override fun check(item: CappedLanePlanThreshold): MutableList<String> {
        val size = item.shiftCode.length
        if (size > maxLength) {
            return mutableListOf<String>("shift code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedLanePlanThresholdNameLengthRule(var maxLength: Int) : CappedLanePlanThresholdRule {
    override fun check(item: CappedLanePlanThreshold): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedLanePlanThresholdAllowedShiftCodeRule(
    var allowed: MutableList<String>,
) : CappedLanePlanThresholdRule {
    override fun check(item: CappedLanePlanThreshold): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.shiftCode))) {
            return mutableListOf<String>("shift code ${item.shiftCode} is blocked")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped lane plan threshold. */
data class CappedLanePlanThresholdValidator(var rules: MutableList<CappedLanePlanThresholdRule>) {
    fun validate(item: CappedLanePlanThreshold): MutableList<String> {
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

    fun requireValid(item: CappedLanePlanThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedLanePlanThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedLanePlanThresholdValidator(): CappedLanePlanThresholdValidator {
    return CappedLanePlanThresholdValidator(
        mutableListOf<CappedLanePlanThresholdRule>(
            CappedLanePlanThresholdRequiredCodeRule("capped lane plan threshold needs this field"),
            CappedLanePlanThresholdShiftCodeLengthRule(32),
            CappedLanePlanThresholdNameLengthRule(32),
            CappedLanePlanThresholdAllowedShiftCodeRule(mutableListOf<String>("fallback", "seasonal")),
        ),
    )
}
