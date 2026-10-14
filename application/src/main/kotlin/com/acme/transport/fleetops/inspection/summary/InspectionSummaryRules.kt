package com.acme.transport.fleetops.inspection.summary

import com.acme.transport.kit.text.normalize

/** A rule a inspection summary must pass. Check returns its problems, or none. */
interface InspectionSummaryRule {
    fun check(item: InspectionSummary): MutableList<String>
}

data class InspectionSummaryTitleLengthRule(var maxLength: Int) : InspectionSummaryRule {
    override fun check(item: InspectionSummary): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class InspectionSummaryAllowedTitleRule(var allowed: MutableList<String>) : InspectionSummaryRule {
    override fun check(item: InspectionSummary): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class InspectionSummaryRequiredShiftCodeRule(var message: String) : InspectionSummaryRule {
    override fun check(item: InspectionSummary): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class InspectionSummaryChannelLengthRule(var maxLength: Int) : InspectionSummaryRule {
    override fun check(item: InspectionSummary): MutableList<String> {
        val size = item.channel.length
        if (size > maxLength) {
            return mutableListOf<String>("channel is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a inspection summary. */
data class InspectionSummaryValidator(var rules: MutableList<InspectionSummaryRule>) {
    fun validate(item: InspectionSummary): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: InspectionSummary) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<InspectionSummary>): MutableMap<String, MutableList<String>> {
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

fun defaultInspectionSummaryValidator(): InspectionSummaryValidator {
    return InspectionSummaryValidator(
        mutableListOf<InspectionSummaryRule>(
            InspectionSummaryTitleLengthRule(80),
            InspectionSummaryAllowedTitleRule(mutableListOf<String>()),
            InspectionSummaryRequiredShiftCodeRule("inspection summary needs this field"),
            InspectionSummaryChannelLengthRule(3600),
        ),
    )
}
