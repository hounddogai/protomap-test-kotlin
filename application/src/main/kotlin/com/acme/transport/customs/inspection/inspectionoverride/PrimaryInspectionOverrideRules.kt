package com.acme.transport.customs.inspection.inspectionoverride

import com.acme.transport.kit.text.normalize

/** A rule a primary inspection override must pass. Check returns its problems, or none. */
interface PrimaryInspectionOverrideRule {
    fun check(item: PrimaryInspectionOverride): MutableList<String>
}

data class PrimaryInspectionOverrideRequiredCodeRule(var message: String) : PrimaryInspectionOverrideRule {
    override fun check(item: PrimaryInspectionOverride): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryInspectionOverrideRequiredChannelRule(var message: String) : PrimaryInspectionOverrideRule {
    override fun check(item: PrimaryInspectionOverride): MutableList<String> {
        if (item.channel.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryInspectionOverrideAllowedContactEmailRule(
    var allowed: MutableList<String>,
) : PrimaryInspectionOverrideRule {
    override fun check(item: PrimaryInspectionOverride): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.contactEmail))) {
            return mutableListOf<String>("contact email ${item.contactEmail} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryInspectionOverrideChannelLengthRule(var maxLength: Int) : PrimaryInspectionOverrideRule {
    override fun check(item: PrimaryInspectionOverride): MutableList<String> {
        val size = item.channel.length
        if (size > maxLength) {
            return mutableListOf<String>("channel is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary inspection override. */
data class PrimaryInspectionOverrideValidator(var rules: MutableList<PrimaryInspectionOverrideRule>) {
    fun validate(item: PrimaryInspectionOverride): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PrimaryInspectionOverride) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryInspectionOverride>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryInspectionOverrideValidator(): PrimaryInspectionOverrideValidator {
    return PrimaryInspectionOverrideValidator(
        mutableListOf<PrimaryInspectionOverrideRule>(
            PrimaryInspectionOverrideRequiredCodeRule("primary inspection override needs an entry"),
            PrimaryInspectionOverrideRequiredChannelRule("primary inspection override needs this field"),
            PrimaryInspectionOverrideAllowedContactEmailRule(mutableListOf<String>("fallback")),
            PrimaryInspectionOverrideChannelLengthRule(86400),
        ),
    )
}
