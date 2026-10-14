package com.acme.transport.crewing.qualification.qualificationmetric

/** A rule a global qualification slot must pass. Check returns its problems, or none. */
interface GlobalQualificationSlotRule {
    fun check(item: GlobalQualificationSlot): MutableList<String>
}

data class GlobalQualificationSlotCodePrefixRule(var prefix: String) : GlobalQualificationSlotRule {
    override fun check(item: GlobalQualificationSlot): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class GlobalQualificationSlotRequiredLabelRule(var message: String) : GlobalQualificationSlotRule {
    override fun check(item: GlobalQualificationSlot): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalQualificationSlotCodeLengthRule(var maxLength: Int) : GlobalQualificationSlotRule {
    override fun check(item: GlobalQualificationSlot): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global qualification slot. */
data class GlobalQualificationSlotValidator(var rules: MutableList<GlobalQualificationSlotRule>) {
    fun validate(item: GlobalQualificationSlot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: GlobalQualificationSlot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalQualificationSlot>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalQualificationSlotValidator(): GlobalQualificationSlotValidator {
    return GlobalQualificationSlotValidator(
        mutableListOf<GlobalQualificationSlotRule>(
            GlobalQualificationSlotCodePrefixRule("Q"),
            GlobalQualificationSlotRequiredLabelRule("global qualification slot needs an entry"),
            GlobalQualificationSlotCodeLengthRule(1000),
        ),
    )
}
