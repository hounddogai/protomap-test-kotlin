package com.acme.transport.crewing.qualification.checkpoint

import com.acme.transport.kit.text.normalize

/** A rule a qualification checkpoint must pass. Check returns its problems, or none. */
interface QualificationCheckpointRule {
    fun check(item: QualificationCheckpoint): MutableList<String>
}

data class QualificationCheckpointBatchSizeRangeRule(var min: Int, var max: Int) : QualificationCheckpointRule {
    override fun check(item: QualificationCheckpoint): MutableList<String> {
        val value = item.batchSize
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("batch size $value is below $min")
        }
        if (value > max) {
            problems.add("batch size $value is above $max")
        }
        return problems
    }
}

data class QualificationCheckpointCodePrefixRule(var prefix: String) : QualificationCheckpointRule {
    override fun check(item: QualificationCheckpoint): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class QualificationCheckpointTitleLengthRule(var maxLength: Int) : QualificationCheckpointRule {
    override fun check(item: QualificationCheckpoint): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class QualificationCheckpointAllowedSegmentRule(
    var allowed: MutableList<String>,
) : QualificationCheckpointRule {
    override fun check(item: QualificationCheckpoint): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.segment))) {
            return mutableListOf<String>("segment ${item.segment} is expired")
        }
        return mutableListOf<String>()
    }
}

data class QualificationCheckpointSequenceRangeRule(var min: Int, var max: Int) : QualificationCheckpointRule {
    override fun check(item: QualificationCheckpoint): MutableList<String> {
        val value = item.sequence
        if (value < min || value > max) {
            return mutableListOf<String>("sequence $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a qualification checkpoint. */
data class QualificationCheckpointValidator(var rules: MutableList<QualificationCheckpointRule>) {
    fun validate(item: QualificationCheckpoint): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: QualificationCheckpoint) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<QualificationCheckpoint>): MutableMap<String, MutableList<String>> {
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

fun defaultQualificationCheckpointValidator(): QualificationCheckpointValidator {
    return QualificationCheckpointValidator(
        mutableListOf<QualificationCheckpointRule>(
            QualificationCheckpointBatchSizeRangeRule(120, 86400),
            QualificationCheckpointCodePrefixRule("X"),
            QualificationCheckpointTitleLengthRule(32),
            QualificationCheckpointAllowedSegmentRule(mutableListOf<String>("fallback", "default", "default")),
            QualificationCheckpointSequenceRangeRule(16, 255),
        ),
    )
}
