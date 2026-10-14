package com.acme.transport.safety.incident.incidentclaim

import com.acme.transport.kit.text.normalize

/** A rule a incident adjustment must pass. Check returns its problems, or none. */
interface IncidentAdjustmentRule {
    fun check(item: IncidentAdjustment): MutableList<String>
}

data class IncidentAdjustmentAllowedReviewerRule(var allowed: MutableList<String>) : IncidentAdjustmentRule {
    override fun check(item: IncidentAdjustment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.reviewer))) {
            return mutableListOf<String>("reviewer ${item.reviewer} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class IncidentAdjustmentCodePrefixRule(var prefix: String) : IncidentAdjustmentRule {
    override fun check(item: IncidentAdjustment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class IncidentAdjustmentTitleLengthRule(var maxLength: Int) : IncidentAdjustmentRule {
    override fun check(item: IncidentAdjustment): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class IncidentAdjustmentRequiredCodeRule(var message: String) : IncidentAdjustmentRule {
    override fun check(item: IncidentAdjustment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class IncidentAdjustmentRequiredLabelRule(var message: String) : IncidentAdjustmentRule {
    override fun check(item: IncidentAdjustment): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class IncidentAdjustmentRequiredTitleRule(var message: String) : IncidentAdjustmentRule {
    override fun check(item: IncidentAdjustment): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a incident adjustment. */
data class IncidentAdjustmentValidator(var rules: MutableList<IncidentAdjustmentRule>) {
    fun validate(item: IncidentAdjustment): MutableList<String> {
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

    fun requireValid(item: IncidentAdjustment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<IncidentAdjustment>): MutableMap<String, MutableList<String>> {
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

fun defaultIncidentAdjustmentValidator(): IncidentAdjustmentValidator {
    return IncidentAdjustmentValidator(
        mutableListOf<IncidentAdjustmentRule>(
            IncidentAdjustmentAllowedReviewerRule(mutableListOf<String>("tiered", "tiered", "legacy")),
            IncidentAdjustmentCodePrefixRule("ACM"),
            IncidentAdjustmentTitleLengthRule(86400),
            IncidentAdjustmentRequiredCodeRule("incident adjustment needs this field"),
            IncidentAdjustmentRequiredLabelRule("incident adjustment needs an entry"),
            IncidentAdjustmentRequiredTitleRule("incident adjustment needs an entry"),
        ),
    )
}
