package com.acme.transport.terminal.sortation.strictsortationdigest

/** A rule a legacy sortation digest must pass. Check returns its problems, or none. */
interface LegacySortationDigestRule {
    fun check(item: LegacySortationDigest): MutableList<String>
}

data class LegacySortationDigestCodePrefixRule(var prefix: String) : LegacySortationDigestRule {
    override fun check(item: LegacySortationDigest): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LegacySortationDigestRequiredCodeRule(var message: String) : LegacySortationDigestRule {
    override fun check(item: LegacySortationDigest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacySortationDigestTitleLengthRule(var maxLength: Int) : LegacySortationDigestRule {
    override fun check(item: LegacySortationDigest): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy sortation digest. */
data class LegacySortationDigestValidator(var rules: MutableList<LegacySortationDigestRule>) {
    fun validate(item: LegacySortationDigest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LegacySortationDigest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacySortationDigest>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacySortationDigestValidator(): LegacySortationDigestValidator {
    return LegacySortationDigestValidator(
        mutableListOf<LegacySortationDigestRule>(
            LegacySortationDigestCodePrefixRule("Q"),
            LegacySortationDigestRequiredCodeRule("legacy sortation digest needs this field"),
            LegacySortationDigestTitleLengthRule(86400),
        ),
    )
}
