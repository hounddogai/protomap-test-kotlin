package com.acme.transport.freightpay.dispute.disputegroup

/** A rule a manual dispute summary must pass. Check returns its problems, or none. */
interface ManualDisputeSummaryRule {
    fun check(item: ManualDisputeSummary): MutableList<String>
}

data class ManualDisputeSummaryNotesLengthRule(var maxLength: Int) : ManualDisputeSummaryRule {
    override fun check(item: ManualDisputeSummary): MutableList<String> {
        val size = item.notes.length
        if (size > maxLength) {
            return mutableListOf<String>("notes is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualDisputeSummaryCodePrefixRule(var prefix: String) : ManualDisputeSummaryRule {
    override fun check(item: ManualDisputeSummary): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ManualDisputeSummaryRequiredNotesRule(var message: String) : ManualDisputeSummaryRule {
    override fun check(item: ManualDisputeSummary): MutableList<String> {
        if (item.notes.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualDisputeSummaryRequiredCodeRule(var message: String) : ManualDisputeSummaryRule {
    override fun check(item: ManualDisputeSummary): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualDisputeSummaryRequiredLabelRule(var message: String) : ManualDisputeSummaryRule {
    override fun check(item: ManualDisputeSummary): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual dispute summary. */
data class ManualDisputeSummaryValidator(var rules: MutableList<ManualDisputeSummaryRule>) {
    fun validate(item: ManualDisputeSummary): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ManualDisputeSummary) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualDisputeSummary>): MutableMap<String, MutableList<String>> {
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

fun defaultManualDisputeSummaryValidator(): ManualDisputeSummaryValidator {
    return ManualDisputeSummaryValidator(
        mutableListOf<ManualDisputeSummaryRule>(
            ManualDisputeSummaryNotesLengthRule(80),
            ManualDisputeSummaryCodePrefixRule("ACM"),
            ManualDisputeSummaryRequiredNotesRule("manual dispute summary needs a value"),
            ManualDisputeSummaryRequiredCodeRule("manual dispute summary needs an entry"),
            ManualDisputeSummaryRequiredLabelRule("manual dispute summary needs an entry"),
        ),
    )
}
