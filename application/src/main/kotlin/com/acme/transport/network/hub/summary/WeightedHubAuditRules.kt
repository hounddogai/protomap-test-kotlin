package com.acme.transport.network.hub.summary

/** A rule a weighted hub audit must pass. Check returns its problems, or none. */
interface WeightedHubAuditRule {
    fun check(item: WeightedHubAudit): MutableList<String>
}

data class WeightedHubAuditRequiredTitleRule(var message: String) : WeightedHubAuditRule {
    override fun check(item: WeightedHubAudit): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedHubAuditRequiredLocaleRule(var message: String) : WeightedHubAuditRule {
    override fun check(item: WeightedHubAudit): MutableList<String> {
        if (item.locale.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedHubAuditTitleLengthRule(var maxLength: Int) : WeightedHubAuditRule {
    override fun check(item: WeightedHubAudit): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted hub audit. */
data class WeightedHubAuditValidator(var rules: MutableList<WeightedHubAuditRule>) {
    fun validate(item: WeightedHubAudit): MutableList<String> {
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

    fun requireValid(item: WeightedHubAudit) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedHubAudit>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedHubAuditValidator(): WeightedHubAuditValidator {
    return WeightedHubAuditValidator(
        mutableListOf<WeightedHubAuditRule>(
            WeightedHubAuditRequiredTitleRule("weighted hub audit needs this field"),
            WeightedHubAuditRequiredLocaleRule("weighted hub audit needs this field"),
            WeightedHubAuditTitleLengthRule(120),
        ),
    )
}
