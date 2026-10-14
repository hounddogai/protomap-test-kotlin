package com.acme.transport.customs.clearance.preference

/** A rule a clearance rollup must pass. Check returns its problems, or none. */
interface ClearanceRollupRule {
    fun check(item: ClearanceRollup): MutableList<String>
}

data class ClearanceRollupContactEmailLengthRule(var maxLength: Int) : ClearanceRollupRule {
    override fun check(item: ClearanceRollup): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ClearanceRollupRequiredContactEmailRule(var message: String) : ClearanceRollupRule {
    override fun check(item: ClearanceRollup): MutableList<String> {
        if (item.contactEmail.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ClearanceRollupActiveGuardRule(var limit: Double) : ClearanceRollupRule {
    override fun check(item: ClearanceRollup): MutableList<String> {
        if (item.active && item.score > limit) {
            return mutableListOf<String>("score ${item.score} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

data class ClearanceRollupRequiredCodeRule(var message: String) : ClearanceRollupRule {
    override fun check(item: ClearanceRollup): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ClearanceRollupCodePrefixRule(var prefix: String) : ClearanceRollupRule {
    override fun check(item: ClearanceRollup): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ClearanceRollupCodeLengthRule(var maxLength: Int) : ClearanceRollupRule {
    override fun check(item: ClearanceRollup): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a clearance rollup. */
data class ClearanceRollupValidator(var rules: MutableList<ClearanceRollupRule>) {
    fun validate(item: ClearanceRollup): MutableList<String> {
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

    fun requireValid(item: ClearanceRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ClearanceRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultClearanceRollupValidator(): ClearanceRollupValidator {
    return ClearanceRollupValidator(
        mutableListOf<ClearanceRollupRule>(
            ClearanceRollupContactEmailLengthRule(3600),
            ClearanceRollupRequiredContactEmailRule("clearance rollup needs an entry"),
            ClearanceRollupActiveGuardRule(3.3),
            ClearanceRollupRequiredCodeRule("clearance rollup needs a value"),
            ClearanceRollupCodePrefixRule("Q"),
            ClearanceRollupCodeLengthRule(86400),
        ),
    )
}
