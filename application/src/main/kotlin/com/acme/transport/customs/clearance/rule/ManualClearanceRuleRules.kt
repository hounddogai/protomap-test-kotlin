package com.acme.transport.customs.clearance.rule

/** A rule a manual clearance rule must pass. Check returns its problems, or none. */
interface ManualClearanceRuleRule {
    fun check(item: ManualClearanceRule): MutableList<String>
}

data class ManualClearanceRuleRequiredLabelRule(var message: String) : ManualClearanceRuleRule {
    override fun check(item: ManualClearanceRule): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualClearanceRuleCodePrefixRule(var prefix: String) : ManualClearanceRuleRule {
    override fun check(item: ManualClearanceRule): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ManualClearanceRuleRequiredCodeRule(var message: String) : ManualClearanceRuleRule {
    override fun check(item: ManualClearanceRule): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualClearanceRuleArchivedGuardRule(var limit: Int) : ManualClearanceRuleRule {
    override fun check(item: ManualClearanceRule): MutableList<String> {
        if (item.archived && item.sequence > limit) {
            return mutableListOf<String>("sequence ${item.sequence} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

data class ManualClearanceRuleCodeLengthRule(var maxLength: Int) : ManualClearanceRuleRule {
    override fun check(item: ManualClearanceRule): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual clearance rule. */
data class ManualClearanceRuleValidator(var rules: MutableList<ManualClearanceRuleRule>) {
    fun validate(item: ManualClearanceRule): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ManualClearanceRule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualClearanceRule>): MutableMap<String, MutableList<String>> {
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

fun defaultManualClearanceRuleValidator(): ManualClearanceRuleValidator {
    return ManualClearanceRuleValidator(
        mutableListOf<ManualClearanceRuleRule>(
            ManualClearanceRuleRequiredLabelRule("manual clearance rule needs a value"),
            ManualClearanceRuleCodePrefixRule("REF"),
            ManualClearanceRuleRequiredCodeRule("manual clearance rule needs this field"),
            ManualClearanceRuleArchivedGuardRule(1000),
            ManualClearanceRuleCodeLengthRule(255),
        ),
    )
}
