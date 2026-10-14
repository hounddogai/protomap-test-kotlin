package com.acme.transport.lastmile.proofofdelivery.audit

import com.acme.transport.kit.text.normalize

/** A rule a scheduled proof of delivery entry must pass. Check returns its problems, or none. */
interface ScheduledProofOfDeliveryEntryRule {
    fun check(item: ScheduledProofOfDeliveryEntry): MutableList<String>
}

data class ScheduledProofOfDeliveryEntryRequiredCodeRule(
    var message: String,
) : ScheduledProofOfDeliveryEntryRule {
    override fun check(item: ScheduledProofOfDeliveryEntry): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledProofOfDeliveryEntryRequiredTitleRule(
    var message: String,
) : ScheduledProofOfDeliveryEntryRule {
    override fun check(item: ScheduledProofOfDeliveryEntry): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledProofOfDeliveryEntryCodePrefixRule(var prefix: String) : ScheduledProofOfDeliveryEntryRule {
    override fun check(item: ScheduledProofOfDeliveryEntry): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledProofOfDeliveryEntryAllowedTitleRule(
    var allowed: MutableList<String>,
) : ScheduledProofOfDeliveryEntryRule {
    override fun check(item: ScheduledProofOfDeliveryEntry): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is above maximum")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled proof of delivery entry. */
data class ScheduledProofOfDeliveryEntryValidator(var rules: MutableList<ScheduledProofOfDeliveryEntryRule>) {
    fun validate(item: ScheduledProofOfDeliveryEntry): MutableList<String> {
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

    fun requireValid(item: ScheduledProofOfDeliveryEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledProofOfDeliveryEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledProofOfDeliveryEntryValidator(): ScheduledProofOfDeliveryEntryValidator {
    return ScheduledProofOfDeliveryEntryValidator(
        mutableListOf<ScheduledProofOfDeliveryEntryRule>(
            ScheduledProofOfDeliveryEntryRequiredCodeRule("scheduled proof of delivery entry needs a value"),
            ScheduledProofOfDeliveryEntryRequiredTitleRule("scheduled proof of delivery entry needs this field"),
            ScheduledProofOfDeliveryEntryCodePrefixRule("X"),
            ScheduledProofOfDeliveryEntryAllowedTitleRule(mutableListOf<String>("fallback", "rolling")),
        ),
    )
}
