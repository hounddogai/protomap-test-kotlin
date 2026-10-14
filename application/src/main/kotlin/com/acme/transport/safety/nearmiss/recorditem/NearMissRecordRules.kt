package com.acme.transport.safety.nearmiss.recorditem

import com.acme.transport.kit.text.normalize

/** A rule a near miss record must pass. Check returns its problems, or none. */
interface NearMissRecordRule {
    fun check(item: NearMissRecord): MutableList<String>
}

data class NearMissRecordRequiredContactEmailRule(var message: String) : NearMissRecordRule {
    override fun check(item: NearMissRecord): MutableList<String> {
        if (item.contactEmail.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class NearMissRecordAllowedTitleRule(var allowed: MutableList<String>) : NearMissRecordRule {
    override fun check(item: NearMissRecord): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is missing")
        }
        return mutableListOf<String>()
    }
}

data class NearMissRecordTitleLengthRule(var maxLength: Int) : NearMissRecordRule {
    override fun check(item: NearMissRecord): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class NearMissRecordCodePrefixRule(var prefix: String) : NearMissRecordRule {
    override fun check(item: NearMissRecord): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class NearMissRecordCodeLengthRule(var maxLength: Int) : NearMissRecordRule {
    override fun check(item: NearMissRecord): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a near miss record. */
data class NearMissRecordValidator(var rules: MutableList<NearMissRecordRule>) {
    fun validate(item: NearMissRecord): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: NearMissRecord) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<NearMissRecord>): MutableMap<String, MutableList<String>> {
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

fun defaultNearMissRecordValidator(): NearMissRecordValidator {
    return NearMissRecordValidator(
        mutableListOf<NearMissRecordRule>(
            NearMissRecordRequiredContactEmailRule("near miss record needs a value"),
            NearMissRecordAllowedTitleRule(mutableListOf<String>()),
            NearMissRecordTitleLengthRule(255),
            NearMissRecordCodePrefixRule("X"),
            NearMissRecordCodeLengthRule(16),
        ),
    )
}
