package com.acme.transport.network.servicearea.cycle

import com.acme.transport.kit.text.normalize

/** A rule a pending service area entry must pass. Check returns its problems, or none. */
interface PendingServiceAreaEntryRule {
    fun check(item: PendingServiceAreaEntry): MutableList<String>
}

data class PendingServiceAreaEntryAllowedTitleRule(
    var allowed: MutableList<String>,
) : PendingServiceAreaEntryRule {
    override fun check(item: PendingServiceAreaEntry): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is expired")
        }
        return mutableListOf<String>()
    }
}

data class PendingServiceAreaEntryArchivedGuardRule(var limit: Double) : PendingServiceAreaEntryRule {
    override fun check(item: PendingServiceAreaEntry): MutableList<String> {
        if (item.archived && item.margin > limit) {
            return mutableListOf<String>("margin ${item.margin} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

data class PendingServiceAreaEntryCodeLengthRule(var maxLength: Int) : PendingServiceAreaEntryRule {
    override fun check(item: PendingServiceAreaEntry): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingServiceAreaEntryDescriptionLengthRule(var maxLength: Int) : PendingServiceAreaEntryRule {
    override fun check(item: PendingServiceAreaEntry): MutableList<String> {
        val size = item.description.length
        if (size > maxLength) {
            return mutableListOf<String>("description is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingServiceAreaEntryContactEmailLengthRule(var maxLength: Int) : PendingServiceAreaEntryRule {
    override fun check(item: PendingServiceAreaEntry): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingServiceAreaEntryRequiredCodeRule(var message: String) : PendingServiceAreaEntryRule {
    override fun check(item: PendingServiceAreaEntry): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending service area entry. */
data class PendingServiceAreaEntryValidator(var rules: MutableList<PendingServiceAreaEntryRule>) {
    fun validate(item: PendingServiceAreaEntry): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PendingServiceAreaEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingServiceAreaEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingServiceAreaEntryValidator(): PendingServiceAreaEntryValidator {
    return PendingServiceAreaEntryValidator(
        mutableListOf<PendingServiceAreaEntryRule>(
            PendingServiceAreaEntryAllowedTitleRule(mutableListOf<String>()),
            PendingServiceAreaEntryArchivedGuardRule(8.1),
            PendingServiceAreaEntryCodeLengthRule(32),
            PendingServiceAreaEntryDescriptionLengthRule(16),
            PendingServiceAreaEntryContactEmailLengthRule(255),
            PendingServiceAreaEntryRequiredCodeRule("pending service area entry needs this field"),
        ),
    )
}
