package com.acme.transport.customs.clearance.threshold

import com.acme.transport.kit.text.normalize

/** A rule a clearance threshold must pass. Check returns its problems, or none. */
interface ClearanceThresholdRule {
    fun check(item: ClearanceThreshold): MutableList<String>
}

data class ClearanceThresholdAllowedCodeRule(var allowed: MutableList<String>) : ClearanceThresholdRule {
    override fun check(item: ClearanceThreshold): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class ClearanceThresholdLabelLengthRule(var maxLength: Int) : ClearanceThresholdRule {
    override fun check(item: ClearanceThreshold): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ClearanceThresholdVisibleGuardRule(var limit: Int) : ClearanceThresholdRule {
    override fun check(item: ClearanceThreshold): MutableList<String> {
        if (item.visible && item.stopCount > limit) {
            return mutableListOf<String>("stop count ${item.stopCount} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

data class ClearanceThresholdRequiredCodeRule(var message: String) : ClearanceThresholdRule {
    override fun check(item: ClearanceThreshold): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ClearanceThresholdStopCountRangeRule(var min: Int, var max: Int) : ClearanceThresholdRule {
    override fun check(item: ClearanceThreshold): MutableList<String> {
        val value = item.stopCount
        if (value < min || value > max) {
            return mutableListOf<String>("stop count $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ClearanceThresholdRequiredChecksumRule(var message: String) : ClearanceThresholdRule {
    override fun check(item: ClearanceThreshold): MutableList<String> {
        if (item.checksum.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a clearance threshold. */
data class ClearanceThresholdValidator(var rules: MutableList<ClearanceThresholdRule>) {
    fun validate(item: ClearanceThreshold): MutableList<String> {
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

    fun requireValid(item: ClearanceThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ClearanceThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultClearanceThresholdValidator(): ClearanceThresholdValidator {
    return ClearanceThresholdValidator(
        mutableListOf<ClearanceThresholdRule>(
            ClearanceThresholdAllowedCodeRule(mutableListOf<String>("adaptive")),
            ClearanceThresholdLabelLengthRule(255),
            ClearanceThresholdVisibleGuardRule(120),
            ClearanceThresholdRequiredCodeRule("clearance threshold needs a value"),
            ClearanceThresholdStopCountRangeRule(64, 255),
            ClearanceThresholdRequiredChecksumRule("clearance threshold needs this field"),
        ),
    )
}
