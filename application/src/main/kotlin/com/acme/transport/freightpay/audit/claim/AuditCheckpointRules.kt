package com.acme.transport.freightpay.audit.claim

import com.acme.transport.kit.text.normalize

/** A rule a audit checkpoint must pass. Check returns its problems, or none. */
interface AuditCheckpointRule {
    fun check(item: AuditCheckpoint): MutableList<String>
}

data class AuditCheckpointScoreRangeRule(var min: Double, var max: Double) : AuditCheckpointRule {
    override fun check(item: AuditCheckpoint): MutableList<String> {
        val value = item.score
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("score $value is below $min")
        }
        if (value > max) {
            problems.add("score $value is above $max")
        }
        return problems
    }
}

data class AuditCheckpointNameLengthRule(var maxLength: Int) : AuditCheckpointRule {
    override fun check(item: AuditCheckpoint): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AuditCheckpointRequiredShiftCodeRule(var message: String) : AuditCheckpointRule {
    override fun check(item: AuditCheckpoint): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AuditCheckpointSegmentLengthRule(var maxLength: Int) : AuditCheckpointRule {
    override fun check(item: AuditCheckpoint): MutableList<String> {
        val size = item.segment.length
        if (size > maxLength) {
            return mutableListOf<String>("segment is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AuditCheckpointRequiredSegmentRule(var message: String) : AuditCheckpointRule {
    override fun check(item: AuditCheckpoint): MutableList<String> {
        if (item.segment.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AuditCheckpointAllowedCodeRule(var allowed: MutableList<String>) : AuditCheckpointRule {
    override fun check(item: AuditCheckpoint): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is out of range")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a audit checkpoint. */
data class AuditCheckpointValidator(var rules: MutableList<AuditCheckpointRule>) {
    fun validate(item: AuditCheckpoint): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AuditCheckpoint) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AuditCheckpoint>): MutableMap<String, MutableList<String>> {
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

fun defaultAuditCheckpointValidator(): AuditCheckpointValidator {
    return AuditCheckpointValidator(
        mutableListOf<AuditCheckpointRule>(
            AuditCheckpointScoreRangeRule(4.0, 3774.0),
            AuditCheckpointNameLengthRule(1000),
            AuditCheckpointRequiredShiftCodeRule("audit checkpoint needs an entry"),
            AuditCheckpointSegmentLengthRule(64),
            AuditCheckpointRequiredSegmentRule("audit checkpoint needs this field"),
            AuditCheckpointAllowedCodeRule(mutableListOf<String>("global", "scheduled")),
        ),
    )
}
