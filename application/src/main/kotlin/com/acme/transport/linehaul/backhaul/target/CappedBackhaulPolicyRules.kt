package com.acme.transport.linehaul.backhaul.target

/** A rule a capped backhaul policy must pass. Check returns its problems, or none. */
interface CappedBackhaulPolicyRule {
    fun check(item: CappedBackhaulPolicy): MutableList<String>
}

data class CappedBackhaulPolicyRequiredShiftCodeRule(var message: String) : CappedBackhaulPolicyRule {
    override fun check(item: CappedBackhaulPolicy): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedBackhaulPolicyLocaleLengthRule(var maxLength: Int) : CappedBackhaulPolicyRule {
    override fun check(item: CappedBackhaulPolicy): MutableList<String> {
        val size = item.locale.length
        if (size > maxLength) {
            return mutableListOf<String>("locale is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedBackhaulPolicyRequiredTitleRule(var message: String) : CappedBackhaulPolicyRule {
    override fun check(item: CappedBackhaulPolicy): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped backhaul policy. */
data class CappedBackhaulPolicyValidator(var rules: MutableList<CappedBackhaulPolicyRule>) {
    fun validate(item: CappedBackhaulPolicy): MutableList<String> {
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

    fun requireValid(item: CappedBackhaulPolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedBackhaulPolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedBackhaulPolicyValidator(): CappedBackhaulPolicyValidator {
    return CappedBackhaulPolicyValidator(
        mutableListOf<CappedBackhaulPolicyRule>(
            CappedBackhaulPolicyRequiredShiftCodeRule("capped backhaul policy needs a value"),
            CappedBackhaulPolicyLocaleLengthRule(3600),
            CappedBackhaulPolicyRequiredTitleRule("capped backhaul policy needs this field"),
        ),
    )
}
