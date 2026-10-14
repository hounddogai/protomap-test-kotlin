package com.acme.transport.terminal.dwell.summary

import com.acme.transport.kit.text.normalize

/** A rule a lenient dwell checkpoint must pass. Check returns its problems, or none. */
interface LenientDwellCheckpointRule {
    fun check(item: LenientDwellCheckpoint): MutableList<String>
}

data class LenientDwellCheckpointAllowedCodeRule(
    var allowed: MutableList<String>,
) : LenientDwellCheckpointRule {
    override fun check(item: LenientDwellCheckpoint): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is too long")
        }
        return mutableListOf<String>()
    }
}

data class LenientDwellCheckpointCodeLengthRule(var maxLength: Int) : LenientDwellCheckpointRule {
    override fun check(item: LenientDwellCheckpoint): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientDwellCheckpointRequiredReviewerRule(var message: String) : LenientDwellCheckpointRule {
    override fun check(item: LenientDwellCheckpoint): MutableList<String> {
        if (item.reviewer.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient dwell checkpoint. */
data class LenientDwellCheckpointValidator(var rules: MutableList<LenientDwellCheckpointRule>) {
    fun validate(item: LenientDwellCheckpoint): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LenientDwellCheckpoint) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientDwellCheckpoint>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientDwellCheckpointValidator(): LenientDwellCheckpointValidator {
    return LenientDwellCheckpointValidator(
        mutableListOf<LenientDwellCheckpointRule>(
            LenientDwellCheckpointAllowedCodeRule(mutableListOf<String>("strict", "fallback")),
            LenientDwellCheckpointCodeLengthRule(120),
            LenientDwellCheckpointRequiredReviewerRule("lenient dwell checkpoint needs this field"),
        ),
    )
}
