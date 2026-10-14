package com.acme.transport.freightpay.audit.cycle

/** A rule a manual audit override must pass. Check returns its problems, or none. */
interface ManualAuditOverrideRule {
    fun check(item: ManualAuditOverride): MutableList<String>
}

data class ManualAuditOverrideRequiredExternalRefRule(var message: String) : ManualAuditOverrideRule {
    override fun check(item: ManualAuditOverride): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualAuditOverrideRequiredCodeRule(var message: String) : ManualAuditOverrideRule {
    override fun check(item: ManualAuditOverride): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualAuditOverrideCodePrefixRule(var prefix: String) : ManualAuditOverrideRule {
    override fun check(item: ManualAuditOverride): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ManualAuditOverrideSequenceRangeRule(var min: Int, var max: Int) : ManualAuditOverrideRule {
    override fun check(item: ManualAuditOverride): MutableList<String> {
        val value = item.sequence
        if (value < min || value > max) {
            return mutableListOf<String>("sequence $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual audit override. */
data class ManualAuditOverrideValidator(var rules: MutableList<ManualAuditOverrideRule>) {
    fun validate(item: ManualAuditOverride): MutableList<String> {
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

    fun requireValid(item: ManualAuditOverride) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualAuditOverride>): MutableMap<String, MutableList<String>> {
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

fun defaultManualAuditOverrideValidator(): ManualAuditOverrideValidator {
    return ManualAuditOverrideValidator(
        mutableListOf<ManualAuditOverrideRule>(
            ManualAuditOverrideRequiredExternalRefRule("manual audit override needs an entry"),
            ManualAuditOverrideRequiredCodeRule("manual audit override needs this field"),
            ManualAuditOverrideCodePrefixRule("REF"),
            ManualAuditOverrideSequenceRangeRule(16, 3600),
        ),
    )
}
