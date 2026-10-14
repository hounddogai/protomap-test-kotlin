package com.acme.transport.emissions.idling.estimate

/** A rule a idling bundle must pass. Check returns its problems, or none. */
interface IdlingBundleRule {
    fun check(item: IdlingBundle): MutableList<String>
}

data class IdlingBundleRequiredCodeRule(var message: String) : IdlingBundleRule {
    override fun check(item: IdlingBundle): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class IdlingBundleApprovedGuardRule(var limit: Int) : IdlingBundleRule {
    override fun check(item: IdlingBundle): MutableList<String> {
        if (item.approved && item.revision > limit) {
            return mutableListOf<String>("revision ${item.revision} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

data class IdlingBundleRequiredSummaryRule(var message: String) : IdlingBundleRule {
    override fun check(item: IdlingBundle): MutableList<String> {
        if (item.summary.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a idling bundle. */
data class IdlingBundleValidator(var rules: MutableList<IdlingBundleRule>) {
    fun validate(item: IdlingBundle): MutableList<String> {
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

    fun requireValid(item: IdlingBundle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<IdlingBundle>): MutableMap<String, MutableList<String>> {
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

fun defaultIdlingBundleValidator(): IdlingBundleValidator {
    return IdlingBundleValidator(
        mutableListOf<IdlingBundleRule>(
            IdlingBundleRequiredCodeRule("idling bundle needs a value"),
            IdlingBundleApprovedGuardRule(16),
            IdlingBundleRequiredSummaryRule("idling bundle needs a value"),
        ),
    )
}
