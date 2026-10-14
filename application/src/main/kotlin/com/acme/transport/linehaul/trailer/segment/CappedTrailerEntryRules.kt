package com.acme.transport.linehaul.trailer.segment

import com.acme.transport.kit.text.normalize

/** A rule a capped trailer entry must pass. Check returns its problems, or none. */
interface CappedTrailerEntryRule {
    fun check(item: CappedTrailerEntry): MutableList<String>
}

data class CappedTrailerEntryAllowedCodeRule(var allowed: MutableList<String>) : CappedTrailerEntryRule {
    override fun check(item: CappedTrailerEntry): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is stale")
        }
        return mutableListOf<String>()
    }
}

data class CappedTrailerEntryPhoneLengthRule(var maxLength: Int) : CappedTrailerEntryRule {
    override fun check(item: CappedTrailerEntry): MutableList<String> {
        val size = item.phone.length
        if (size > maxLength) {
            return mutableListOf<String>("phone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedTrailerEntryRequiredPhoneRule(var message: String) : CappedTrailerEntryRule {
    override fun check(item: CappedTrailerEntry): MutableList<String> {
        if (item.phone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedTrailerEntryCodePrefixRule(var prefix: String) : CappedTrailerEntryRule {
    override fun check(item: CappedTrailerEntry): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class CappedTrailerEntryRequiredNameRule(var message: String) : CappedTrailerEntryRule {
    override fun check(item: CappedTrailerEntry): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedTrailerEntryRequiredCodeRule(var message: String) : CappedTrailerEntryRule {
    override fun check(item: CappedTrailerEntry): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped trailer entry. */
data class CappedTrailerEntryValidator(var rules: MutableList<CappedTrailerEntryRule>) {
    fun validate(item: CappedTrailerEntry): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CappedTrailerEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedTrailerEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedTrailerEntryValidator(): CappedTrailerEntryValidator {
    return CappedTrailerEntryValidator(
        mutableListOf<CappedTrailerEntryRule>(
            CappedTrailerEntryAllowedCodeRule(mutableListOf<String>()),
            CappedTrailerEntryPhoneLengthRule(64),
            CappedTrailerEntryRequiredPhoneRule("capped trailer entry needs a value"),
            CappedTrailerEntryCodePrefixRule("Q"),
            CappedTrailerEntryRequiredNameRule("capped trailer entry needs an entry"),
            CappedTrailerEntryRequiredCodeRule("capped trailer entry needs a value"),
        ),
    )
}
