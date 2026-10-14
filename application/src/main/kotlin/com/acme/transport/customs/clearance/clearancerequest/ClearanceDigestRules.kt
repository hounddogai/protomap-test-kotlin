package com.acme.transport.customs.clearance.clearancerequest

/** A rule a clearance digest must pass. Check returns its problems, or none. */
interface ClearanceDigestRule {
    fun check(item: ClearanceDigest): MutableList<String>
}

data class ClearanceDigestExternalRefLengthRule(var maxLength: Int) : ClearanceDigestRule {
    override fun check(item: ClearanceDigest): MutableList<String> {
        val size = item.externalRef.length
        if (size > maxLength) {
            return mutableListOf<String>("external ref is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ClearanceDigestRequiredCodeRule(var message: String) : ClearanceDigestRule {
    override fun check(item: ClearanceDigest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ClearanceDigestCodeLengthRule(var maxLength: Int) : ClearanceDigestRule {
    override fun check(item: ClearanceDigest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ClearanceDigestCodePrefixRule(var prefix: String) : ClearanceDigestRule {
    override fun check(item: ClearanceDigest): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ClearanceDigestRequiredTitleRule(var message: String) : ClearanceDigestRule {
    override fun check(item: ClearanceDigest): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a clearance digest. */
data class ClearanceDigestValidator(var rules: MutableList<ClearanceDigestRule>) {
    fun validate(item: ClearanceDigest): MutableList<String> {
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

    fun requireValid(item: ClearanceDigest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ClearanceDigest>): MutableMap<String, MutableList<String>> {
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

fun defaultClearanceDigestValidator(): ClearanceDigestValidator {
    return ClearanceDigestValidator(
        mutableListOf<ClearanceDigestRule>(
            ClearanceDigestExternalRefLengthRule(86400),
            ClearanceDigestRequiredCodeRule("clearance digest needs a value"),
            ClearanceDigestCodeLengthRule(80),
            ClearanceDigestCodePrefixRule("Q"),
            ClearanceDigestRequiredTitleRule("clearance digest needs a value"),
        ),
    )
}
