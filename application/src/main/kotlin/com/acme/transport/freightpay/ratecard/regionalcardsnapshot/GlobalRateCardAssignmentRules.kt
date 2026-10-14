package com.acme.transport.freightpay.ratecard.regionalcardsnapshot

/** A rule a global rate card assignment must pass. Check returns its problems, or none. */
interface GlobalRateCardAssignmentRule {
    fun check(item: GlobalRateCardAssignment): MutableList<String>
}

data class GlobalRateCardAssignmentTitleLengthRule(var maxLength: Int) : GlobalRateCardAssignmentRule {
    override fun check(item: GlobalRateCardAssignment): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalRateCardAssignmentCodePrefixRule(var prefix: String) : GlobalRateCardAssignmentRule {
    override fun check(item: GlobalRateCardAssignment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class GlobalRateCardAssignmentRequiredNameRule(var message: String) : GlobalRateCardAssignmentRule {
    override fun check(item: GlobalRateCardAssignment): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global rate card assignment. */
data class GlobalRateCardAssignmentValidator(var rules: MutableList<GlobalRateCardAssignmentRule>) {
    fun validate(item: GlobalRateCardAssignment): MutableList<String> {
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

    fun requireValid(item: GlobalRateCardAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalRateCardAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalRateCardAssignmentValidator(): GlobalRateCardAssignmentValidator {
    return GlobalRateCardAssignmentValidator(
        mutableListOf<GlobalRateCardAssignmentRule>(
            GlobalRateCardAssignmentTitleLengthRule(120),
            GlobalRateCardAssignmentCodePrefixRule("ACM"),
            GlobalRateCardAssignmentRequiredNameRule("global rate card assignment needs a value"),
        ),
    )
}
