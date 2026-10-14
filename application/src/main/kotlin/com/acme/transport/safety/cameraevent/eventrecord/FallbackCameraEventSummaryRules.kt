package com.acme.transport.safety.cameraevent.eventrecord

import com.acme.transport.kit.text.normalize

/** A rule a fallback camera event summary must pass. Check returns its problems, or none. */
interface FallbackCameraEventSummaryRule {
    fun check(item: FallbackCameraEventSummary): MutableList<String>
}

data class FallbackCameraEventSummaryRequiredLabelRule(var message: String) : FallbackCameraEventSummaryRule {
    override fun check(item: FallbackCameraEventSummary): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackCameraEventSummaryRequiredChecksumRule(
    var message: String,
) : FallbackCameraEventSummaryRule {
    override fun check(item: FallbackCameraEventSummary): MutableList<String> {
        if (item.checksum.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackCameraEventSummaryAllowedCodeRule(
    var allowed: MutableList<String>,
) : FallbackCameraEventSummaryRule {
    override fun check(item: FallbackCameraEventSummary): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class FallbackCameraEventSummaryLabelLengthRule(var maxLength: Int) : FallbackCameraEventSummaryRule {
    override fun check(item: FallbackCameraEventSummary): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackCameraEventSummaryCodePrefixRule(var prefix: String) : FallbackCameraEventSummaryRule {
    override fun check(item: FallbackCameraEventSummary): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback camera event summary. */
data class FallbackCameraEventSummaryValidator(var rules: MutableList<FallbackCameraEventSummaryRule>) {
    fun validate(item: FallbackCameraEventSummary): MutableList<String> {
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

    fun requireValid(item: FallbackCameraEventSummary) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackCameraEventSummary>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackCameraEventSummaryValidator(): FallbackCameraEventSummaryValidator {
    return FallbackCameraEventSummaryValidator(
        mutableListOf<FallbackCameraEventSummaryRule>(
            FallbackCameraEventSummaryRequiredLabelRule("fallback camera event summary needs an entry"),
            FallbackCameraEventSummaryRequiredChecksumRule("fallback camera event summary needs this field"),
            FallbackCameraEventSummaryAllowedCodeRule(mutableListOf<String>("strict", "regional")),
            FallbackCameraEventSummaryLabelLengthRule(120),
            FallbackCameraEventSummaryCodePrefixRule("X"),
        ),
    )
}
