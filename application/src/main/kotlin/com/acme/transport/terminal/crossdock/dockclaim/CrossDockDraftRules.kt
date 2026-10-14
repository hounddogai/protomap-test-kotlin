package com.acme.transport.terminal.crossdock.dockclaim

import com.acme.transport.kit.text.normalize

/** A rule a cross dock draft must pass. Check returns its problems, or none. */
interface CrossDockDraftRule {
    fun check(item: CrossDockDraft): MutableList<String>
}

data class CrossDockDraftRequiredPhoneRule(var message: String) : CrossDockDraftRule {
    override fun check(item: CrossDockDraft): MutableList<String> {
        if (item.phone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CrossDockDraftAllowedPhoneRule(var allowed: MutableList<String>) : CrossDockDraftRule {
    override fun check(item: CrossDockDraft): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.phone))) {
            return mutableListOf<String>("phone ${item.phone} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class CrossDockDraftRequiredCodeRule(var message: String) : CrossDockDraftRule {
    override fun check(item: CrossDockDraft): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CrossDockDraftCodePrefixRule(var prefix: String) : CrossDockDraftRule {
    override fun check(item: CrossDockDraft): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class CrossDockDraftTitleLengthRule(var maxLength: Int) : CrossDockDraftRule {
    override fun check(item: CrossDockDraft): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a cross dock draft. */
data class CrossDockDraftValidator(var rules: MutableList<CrossDockDraftRule>) {
    fun validate(item: CrossDockDraft): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CrossDockDraft) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CrossDockDraft>): MutableMap<String, MutableList<String>> {
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

fun defaultCrossDockDraftValidator(): CrossDockDraftValidator {
    return CrossDockDraftValidator(
        mutableListOf<CrossDockDraftRule>(
            CrossDockDraftRequiredPhoneRule("cross dock draft needs this field"),
            CrossDockDraftAllowedPhoneRule(mutableListOf<String>("default", "capped", "default")),
            CrossDockDraftRequiredCodeRule("cross dock draft needs this field"),
            CrossDockDraftCodePrefixRule("REF"),
            CrossDockDraftTitleLengthRule(120),
        ),
    )
}
