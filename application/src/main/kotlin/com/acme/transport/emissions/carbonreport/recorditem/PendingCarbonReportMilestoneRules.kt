package com.acme.transport.emissions.carbonreport.recorditem

/** A rule a pending carbon report milestone must pass. Check returns its problems, or none. */
interface PendingCarbonReportMilestoneRule {
    fun check(item: PendingCarbonReportMilestone): MutableList<String>
}

data class PendingCarbonReportMilestoneCodePrefixRule(var prefix: String) : PendingCarbonReportMilestoneRule {
    override fun check(item: PendingCarbonReportMilestone): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class PendingCarbonReportMilestoneRequiredChannelRule(
    var message: String,
) : PendingCarbonReportMilestoneRule {
    override fun check(item: PendingCarbonReportMilestone): MutableList<String> {
        if (item.channel.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingCarbonReportMilestoneTitleLengthRule(var maxLength: Int) : PendingCarbonReportMilestoneRule {
    override fun check(item: PendingCarbonReportMilestone): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending carbon report milestone. */
data class PendingCarbonReportMilestoneValidator(var rules: MutableList<PendingCarbonReportMilestoneRule>) {
    fun validate(item: PendingCarbonReportMilestone): MutableList<String> {
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

    fun requireValid(item: PendingCarbonReportMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingCarbonReportMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingCarbonReportMilestoneValidator(): PendingCarbonReportMilestoneValidator {
    return PendingCarbonReportMilestoneValidator(
        mutableListOf<PendingCarbonReportMilestoneRule>(
            PendingCarbonReportMilestoneCodePrefixRule("ACM"),
            PendingCarbonReportMilestoneRequiredChannelRule("pending carbon report milestone needs an entry"),
            PendingCarbonReportMilestoneTitleLengthRule(64),
        ),
    )
}
