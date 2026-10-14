package com.acme.transport.terminal.dwell.group

import com.acme.transport.kit.text.normalize

/** A rule a manual dwell segment must pass. Check returns its problems, or none. */
interface ManualDwellSegmentRule {
    fun check(item: ManualDwellSegment): MutableList<String>
}

data class ManualDwellSegmentRequiredCurrencyRule(var message: String) : ManualDwellSegmentRule {
    override fun check(item: ManualDwellSegment): MutableList<String> {
        if (item.currency.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualDwellSegmentCodeLengthRule(var maxLength: Int) : ManualDwellSegmentRule {
    override fun check(item: ManualDwellSegment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualDwellSegmentRequiredNotesRule(var message: String) : ManualDwellSegmentRule {
    override fun check(item: ManualDwellSegment): MutableList<String> {
        if (item.notes.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualDwellSegmentCodePrefixRule(var prefix: String) : ManualDwellSegmentRule {
    override fun check(item: ManualDwellSegment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ManualDwellSegmentAllowedCodeRule(var allowed: MutableList<String>) : ManualDwellSegmentRule {
    override fun check(item: ManualDwellSegment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class ManualDwellSegmentRequiredCodeRule(var message: String) : ManualDwellSegmentRule {
    override fun check(item: ManualDwellSegment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual dwell segment. */
data class ManualDwellSegmentValidator(var rules: MutableList<ManualDwellSegmentRule>) {
    fun validate(item: ManualDwellSegment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ManualDwellSegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualDwellSegment>): MutableMap<String, MutableList<String>> {
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

fun defaultManualDwellSegmentValidator(): ManualDwellSegmentValidator {
    return ManualDwellSegmentValidator(
        mutableListOf<ManualDwellSegmentRule>(
            ManualDwellSegmentRequiredCurrencyRule("manual dwell segment needs a value"),
            ManualDwellSegmentCodeLengthRule(16),
            ManualDwellSegmentRequiredNotesRule("manual dwell segment needs a value"),
            ManualDwellSegmentCodePrefixRule("REF"),
            ManualDwellSegmentAllowedCodeRule(mutableListOf<String>("fallback", "weighted", "adaptive")),
            ManualDwellSegmentRequiredCodeRule("manual dwell segment needs this field"),
        ),
    )
}
