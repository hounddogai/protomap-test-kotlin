package com.acme.transport.safety.nearmiss.manifest

import com.acme.transport.kit.text.normalize

/** A rule a near miss threshold must pass. Check returns its problems, or none. */
interface NearMissThresholdRule {
    fun check(item: NearMissThreshold): MutableList<String>
}

data class NearMissThresholdRequiredUnitRule(var message: String) : NearMissThresholdRule {
    override fun check(item: NearMissThreshold): MutableList<String> {
        if (item.unit.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class NearMissThresholdAllowedCodeRule(var allowed: MutableList<String>) : NearMissThresholdRule {
    override fun check(item: NearMissThreshold): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is too long")
        }
        return mutableListOf<String>()
    }
}

data class NearMissThresholdSequenceRangeRule(var min: Int, var max: Int) : NearMissThresholdRule {
    override fun check(item: NearMissThreshold): MutableList<String> {
        val value = item.sequence
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("sequence $value is below $min")
        }
        if (value > max) {
            problems.add("sequence $value is above $max")
        }
        return problems
    }
}

data class NearMissThresholdRequiredShiftCodeRule(var message: String) : NearMissThresholdRule {
    override fun check(item: NearMissThreshold): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class NearMissThresholdTitleLengthRule(var maxLength: Int) : NearMissThresholdRule {
    override fun check(item: NearMissThreshold): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class NearMissThresholdUnitLengthRule(var maxLength: Int) : NearMissThresholdRule {
    override fun check(item: NearMissThreshold): MutableList<String> {
        val size = item.unit.length
        if (size > maxLength) {
            return mutableListOf<String>("unit is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a near miss threshold. */
data class NearMissThresholdValidator(var rules: MutableList<NearMissThresholdRule>) {
    fun validate(item: NearMissThreshold): MutableList<String> {
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

    fun requireValid(item: NearMissThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<NearMissThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultNearMissThresholdValidator(): NearMissThresholdValidator {
    return NearMissThresholdValidator(
        mutableListOf<NearMissThresholdRule>(
            NearMissThresholdRequiredUnitRule("near miss threshold needs an entry"),
            NearMissThresholdAllowedCodeRule(mutableListOf<String>()),
            NearMissThresholdSequenceRangeRule(32, 32),
            NearMissThresholdRequiredShiftCodeRule("near miss threshold needs a value"),
            NearMissThresholdTitleLengthRule(120),
            NearMissThresholdUnitLengthRule(64),
        ),
    )
}
