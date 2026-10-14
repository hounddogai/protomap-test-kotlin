package com.acme.transport.network.servicearea.quota

/** A rule a primary service area quota must pass. Check returns its problems, or none. */
interface PrimaryServiceAreaQuotaRule {
    fun check(item: PrimaryServiceAreaQuota): MutableList<String>
}

data class PrimaryServiceAreaQuotaRequiredCodeRule(var message: String) : PrimaryServiceAreaQuotaRule {
    override fun check(item: PrimaryServiceAreaQuota): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryServiceAreaQuotaNotesLengthRule(var maxLength: Int) : PrimaryServiceAreaQuotaRule {
    override fun check(item: PrimaryServiceAreaQuota): MutableList<String> {
        val size = item.notes.length
        if (size > maxLength) {
            return mutableListOf<String>("notes is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryServiceAreaQuotaRequiredNotesRule(var message: String) : PrimaryServiceAreaQuotaRule {
    override fun check(item: PrimaryServiceAreaQuota): MutableList<String> {
        if (item.notes.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryServiceAreaQuotaRequiredLabelRule(var message: String) : PrimaryServiceAreaQuotaRule {
    override fun check(item: PrimaryServiceAreaQuota): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary service area quota. */
data class PrimaryServiceAreaQuotaValidator(var rules: MutableList<PrimaryServiceAreaQuotaRule>) {
    fun validate(item: PrimaryServiceAreaQuota): MutableList<String> {
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

    fun requireValid(item: PrimaryServiceAreaQuota) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryServiceAreaQuota>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryServiceAreaQuotaValidator(): PrimaryServiceAreaQuotaValidator {
    return PrimaryServiceAreaQuotaValidator(
        mutableListOf<PrimaryServiceAreaQuotaRule>(
            PrimaryServiceAreaQuotaRequiredCodeRule("primary service area quota needs an entry"),
            PrimaryServiceAreaQuotaNotesLengthRule(255),
            PrimaryServiceAreaQuotaRequiredNotesRule("primary service area quota needs a value"),
            PrimaryServiceAreaQuotaRequiredLabelRule("primary service area quota needs this field"),
        ),
    )
}
