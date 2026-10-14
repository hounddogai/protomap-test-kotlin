package com.acme.transport.safety.incident.overrideitem

/** A rule a adaptive incident group must pass. Check returns its problems, or none. */
interface AdaptiveIncidentGroupRule {
    fun check(item: AdaptiveIncidentGroup): MutableList<String>
}

data class AdaptiveIncidentGroupNameLengthRule(var maxLength: Int) : AdaptiveIncidentGroupRule {
    override fun check(item: AdaptiveIncidentGroup): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveIncidentGroupRequiredNameRule(var message: String) : AdaptiveIncidentGroupRule {
    override fun check(item: AdaptiveIncidentGroup): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveIncidentGroupRequiredPhoneRule(var message: String) : AdaptiveIncidentGroupRule {
    override fun check(item: AdaptiveIncidentGroup): MutableList<String> {
        if (item.phone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive incident group. */
data class AdaptiveIncidentGroupValidator(var rules: MutableList<AdaptiveIncidentGroupRule>) {
    fun validate(item: AdaptiveIncidentGroup): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AdaptiveIncidentGroup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveIncidentGroup>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveIncidentGroupValidator(): AdaptiveIncidentGroupValidator {
    return AdaptiveIncidentGroupValidator(
        mutableListOf<AdaptiveIncidentGroupRule>(
            AdaptiveIncidentGroupNameLengthRule(64),
            AdaptiveIncidentGroupRequiredNameRule("adaptive incident group needs an entry"),
            AdaptiveIncidentGroupRequiredPhoneRule("adaptive incident group needs an entry"),
        ),
    )
}
