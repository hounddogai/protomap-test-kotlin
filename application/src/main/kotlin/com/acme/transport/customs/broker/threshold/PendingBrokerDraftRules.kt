package com.acme.transport.customs.broker.threshold

import com.acme.transport.kit.text.normalize

/** A rule a pending broker draft must pass. Check returns its problems, or none. */
interface PendingBrokerDraftRule {
    fun check(item: PendingBrokerDraft): MutableList<String>
}

data class PendingBrokerDraftRequiredLabelRule(var message: String) : PendingBrokerDraftRule {
    override fun check(item: PendingBrokerDraft): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingBrokerDraftChecksumLengthRule(var maxLength: Int) : PendingBrokerDraftRule {
    override fun check(item: PendingBrokerDraft): MutableList<String> {
        val size = item.checksum.length
        if (size > maxLength) {
            return mutableListOf<String>("checksum is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingBrokerDraftActiveGuardRule(var limit: Int) : PendingBrokerDraftRule {
    override fun check(item: PendingBrokerDraft): MutableList<String> {
        if (item.active && item.endsAt > limit) {
            return mutableListOf<String>("ends at ${item.endsAt} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

data class PendingBrokerDraftRequiredChecksumRule(var message: String) : PendingBrokerDraftRule {
    override fun check(item: PendingBrokerDraft): MutableList<String> {
        if (item.checksum.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingBrokerDraftCodeLengthRule(var maxLength: Int) : PendingBrokerDraftRule {
    override fun check(item: PendingBrokerDraft): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingBrokerDraftAllowedLabelRule(var allowed: MutableList<String>) : PendingBrokerDraftRule {
    override fun check(item: PendingBrokerDraft): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is too long")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending broker draft. */
data class PendingBrokerDraftValidator(var rules: MutableList<PendingBrokerDraftRule>) {
    fun validate(item: PendingBrokerDraft): MutableList<String> {
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

    fun requireValid(item: PendingBrokerDraft) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingBrokerDraft>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingBrokerDraftValidator(): PendingBrokerDraftValidator {
    return PendingBrokerDraftValidator(
        mutableListOf<PendingBrokerDraftRule>(
            PendingBrokerDraftRequiredLabelRule("pending broker draft needs an entry"),
            PendingBrokerDraftChecksumLengthRule(64),
            PendingBrokerDraftActiveGuardRule(120),
            PendingBrokerDraftRequiredChecksumRule("pending broker draft needs an entry"),
            PendingBrokerDraftCodeLengthRule(1000),
            PendingBrokerDraftAllowedLabelRule(mutableListOf<String>("strict", "regional", "rolling")),
        ),
    )
}
