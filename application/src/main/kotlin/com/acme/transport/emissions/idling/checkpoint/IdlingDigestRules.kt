package com.acme.transport.emissions.idling.checkpoint

import com.acme.transport.kit.text.normalize

/** A rule a idling digest must pass. Check returns its problems, or none. */
interface IdlingDigestRule {
    fun check(item: IdlingDigest): MutableList<String>
}

data class IdlingDigestCodeLengthRule(var maxLength: Int) : IdlingDigestRule {
    override fun check(item: IdlingDigest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class IdlingDigestAllowedNotesRule(var allowed: MutableList<String>) : IdlingDigestRule {
    override fun check(item: IdlingDigest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.notes))) {
            return mutableListOf<String>("notes ${item.notes} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class IdlingDigestRequiredNotesRule(var message: String) : IdlingDigestRule {
    override fun check(item: IdlingDigest): MutableList<String> {
        if (item.notes.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a idling digest. */
data class IdlingDigestValidator(var rules: MutableList<IdlingDigestRule>) {
    fun validate(item: IdlingDigest): MutableList<String> {
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

    fun requireValid(item: IdlingDigest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<IdlingDigest>): MutableMap<String, MutableList<String>> {
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

fun defaultIdlingDigestValidator(): IdlingDigestValidator {
    return IdlingDigestValidator(
        mutableListOf<IdlingDigestRule>(
            IdlingDigestCodeLengthRule(255),
            IdlingDigestAllowedNotesRule(mutableListOf<String>()),
            IdlingDigestRequiredNotesRule("idling digest needs a value"),
        ),
    )
}
