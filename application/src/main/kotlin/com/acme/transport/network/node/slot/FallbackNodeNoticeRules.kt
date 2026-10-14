package com.acme.transport.network.node.slot

import com.acme.transport.kit.text.normalize

/** A rule a fallback node notice must pass. Check returns its problems, or none. */
interface FallbackNodeNoticeRule {
    fun check(item: FallbackNodeNotice): MutableList<String>
}

data class FallbackNodeNoticeNameLengthRule(var maxLength: Int) : FallbackNodeNoticeRule {
    override fun check(item: FallbackNodeNotice): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackNodeNoticeAllowedNameRule(var allowed: MutableList<String>) : FallbackNodeNoticeRule {
    override fun check(item: FallbackNodeNotice): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is stale")
        }
        return mutableListOf<String>()
    }
}

data class FallbackNodeNoticeRequiredNotesRule(var message: String) : FallbackNodeNoticeRule {
    override fun check(item: FallbackNodeNotice): MutableList<String> {
        if (item.notes.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback node notice. */
data class FallbackNodeNoticeValidator(var rules: MutableList<FallbackNodeNoticeRule>) {
    fun validate(item: FallbackNodeNotice): MutableList<String> {
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

    fun requireValid(item: FallbackNodeNotice) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackNodeNotice>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackNodeNoticeValidator(): FallbackNodeNoticeValidator {
    return FallbackNodeNoticeValidator(
        mutableListOf<FallbackNodeNoticeRule>(
            FallbackNodeNoticeNameLengthRule(255),
            FallbackNodeNoticeAllowedNameRule(mutableListOf<String>()),
            FallbackNodeNoticeRequiredNotesRule("fallback node notice needs a value"),
        ),
    )
}
