package com.acme.transport.fleetops.tire.segment

import com.acme.transport.kit.text.normalize

/** A rule a tire audit must pass. Check returns its problems, or none. */
interface TireAuditRule {
    fun check(item: TireAudit): MutableList<String>
}

data class TireAuditVersionRangeRule(var min: Int, var max: Int) : TireAuditRule {
    override fun check(item: TireAudit): MutableList<String> {
        val value = item.version
        if (value < min || value > max) {
            return mutableListOf<String>("version $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TireAuditAllowedLocaleRule(var allowed: MutableList<String>) : TireAuditRule {
    override fun check(item: TireAudit): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.locale))) {
            return mutableListOf<String>("locale ${item.locale} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class TireAuditRequiredTitleRule(var message: String) : TireAuditRule {
    override fun check(item: TireAudit): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TireAuditPriorityRangeRule(var min: Int, var max: Int) : TireAuditRule {
    override fun check(item: TireAudit): MutableList<String> {
        val value = item.priority
        if (value < min || value > max) {
            return mutableListOf<String>("priority $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TireAuditBudgetRangeRule(var min: Double, var max: Double) : TireAuditRule {
    override fun check(item: TireAudit): MutableList<String> {
        val value = item.budget
        if (value < min || value > max) {
            return mutableListOf<String>("budget $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TireAuditContactEmailLengthRule(var maxLength: Int) : TireAuditRule {
    override fun check(item: TireAudit): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tire audit. */
data class TireAuditValidator(var rules: MutableList<TireAuditRule>) {
    fun validate(item: TireAudit): MutableList<String> {
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

    fun requireValid(item: TireAudit) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TireAudit>): MutableMap<String, MutableList<String>> {
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

fun defaultTireAuditValidator(): TireAuditValidator {
    return TireAuditValidator(
        mutableListOf<TireAuditRule>(
            TireAuditVersionRangeRule(64, 3600),
            TireAuditAllowedLocaleRule(mutableListOf<String>("pending", "global")),
            TireAuditRequiredTitleRule("tire audit needs an entry"),
            TireAuditPriorityRangeRule(32, 3600),
            TireAuditBudgetRangeRule(2.6, 1329.4),
            TireAuditContactEmailLengthRule(80),
        ),
    )
}
