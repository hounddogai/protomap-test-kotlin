package com.acme.transport.linehaul.relay.assignment

/** A rule a adaptive relay assignment must pass. Check returns its problems, or none. */
interface AdaptiveRelayAssignmentRule {
    fun check(item: AdaptiveRelayAssignment): MutableList<String>
}

data class AdaptiveRelayAssignmentRouteCodeLengthRule(var maxLength: Int) : AdaptiveRelayAssignmentRule {
    override fun check(item: AdaptiveRelayAssignment): MutableList<String> {
        val size = item.routeCode.length
        if (size > maxLength) {
            return mutableListOf<String>("route code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveRelayAssignmentRequiredContactEmailRule(var message: String) : AdaptiveRelayAssignmentRule {
    override fun check(item: AdaptiveRelayAssignment): MutableList<String> {
        if (item.contactEmail.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveRelayAssignmentTitleLengthRule(var maxLength: Int) : AdaptiveRelayAssignmentRule {
    override fun check(item: AdaptiveRelayAssignment): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive relay assignment. */
data class AdaptiveRelayAssignmentValidator(var rules: MutableList<AdaptiveRelayAssignmentRule>) {
    fun validate(item: AdaptiveRelayAssignment): MutableList<String> {
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

    fun requireValid(item: AdaptiveRelayAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveRelayAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveRelayAssignmentValidator(): AdaptiveRelayAssignmentValidator {
    return AdaptiveRelayAssignmentValidator(
        mutableListOf<AdaptiveRelayAssignmentRule>(
            AdaptiveRelayAssignmentRouteCodeLengthRule(16),
            AdaptiveRelayAssignmentRequiredContactEmailRule("adaptive relay assignment needs a value"),
            AdaptiveRelayAssignmentTitleLengthRule(3600),
        ),
    )
}
