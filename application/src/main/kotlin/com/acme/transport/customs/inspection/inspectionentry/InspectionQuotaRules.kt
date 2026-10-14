package com.acme.transport.customs.inspection.inspectionentry

/** A rule a inspection quota must pass. Check returns its problems, or none. */
interface InspectionQuotaRule {
    fun check(item: InspectionQuota): MutableList<String>
}

data class InspectionQuotaRequiredLabelRule(var message: String) : InspectionQuotaRule {
    override fun check(item: InspectionQuota): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class InspectionQuotaRequiredCodeRule(var message: String) : InspectionQuotaRule {
    override fun check(item: InspectionQuota): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class InspectionQuotaRequiredTrailerIdRule(var message: String) : InspectionQuotaRule {
    override fun check(item: InspectionQuota): MutableList<String> {
        if (item.trailerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class InspectionQuotaCodePrefixRule(var prefix: String) : InspectionQuotaRule {
    override fun check(item: InspectionQuota): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class InspectionQuotaLabelLengthRule(var maxLength: Int) : InspectionQuotaRule {
    override fun check(item: InspectionQuota): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a inspection quota. */
data class InspectionQuotaValidator(var rules: MutableList<InspectionQuotaRule>) {
    fun validate(item: InspectionQuota): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: InspectionQuota) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<InspectionQuota>): MutableMap<String, MutableList<String>> {
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

fun defaultInspectionQuotaValidator(): InspectionQuotaValidator {
    return InspectionQuotaValidator(
        mutableListOf<InspectionQuotaRule>(
            InspectionQuotaRequiredLabelRule("inspection quota needs a value"),
            InspectionQuotaRequiredCodeRule("inspection quota needs an entry"),
            InspectionQuotaRequiredTrailerIdRule("inspection quota needs an entry"),
            InspectionQuotaCodePrefixRule("REF"),
            InspectionQuotaLabelLengthRule(64),
        ),
    )
}
