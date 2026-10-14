package com.acme.transport.lastmile.locker.window

/** A rule a capped locker policy must pass. Check returns its problems, or none. */
interface CappedLockerPolicyRule {
    fun check(item: CappedLockerPolicy): MutableList<String>
}

data class CappedLockerPolicyCodePrefixRule(var prefix: String) : CappedLockerPolicyRule {
    override fun check(item: CappedLockerPolicy): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class CappedLockerPolicyRequiredCodeRule(var message: String) : CappedLockerPolicyRule {
    override fun check(item: CappedLockerPolicy): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedLockerPolicySourceLengthRule(var maxLength: Int) : CappedLockerPolicyRule {
    override fun check(item: CappedLockerPolicy): MutableList<String> {
        val size = item.source.length
        if (size > maxLength) {
            return mutableListOf<String>("source is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedLockerPolicyCodeLengthRule(var maxLength: Int) : CappedLockerPolicyRule {
    override fun check(item: CappedLockerPolicy): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedLockerPolicyOwnerIdLengthRule(var maxLength: Int) : CappedLockerPolicyRule {
    override fun check(item: CappedLockerPolicy): MutableList<String> {
        val size = item.ownerId.length
        if (size > maxLength) {
            return mutableListOf<String>("owner id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped locker policy. */
data class CappedLockerPolicyValidator(var rules: MutableList<CappedLockerPolicyRule>) {
    fun validate(item: CappedLockerPolicy): MutableList<String> {
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

    fun requireValid(item: CappedLockerPolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedLockerPolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedLockerPolicyValidator(): CappedLockerPolicyValidator {
    return CappedLockerPolicyValidator(
        mutableListOf<CappedLockerPolicyRule>(
            CappedLockerPolicyCodePrefixRule("Q"),
            CappedLockerPolicyRequiredCodeRule("capped locker policy needs this field"),
            CappedLockerPolicySourceLengthRule(3600),
            CappedLockerPolicyCodeLengthRule(3600),
            CappedLockerPolicyOwnerIdLengthRule(86400),
        ),
    )
}
