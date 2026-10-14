package com.acme.transport.linehaul.tender.summary

/** A rule a tender summary must pass. Check returns its problems, or none. */
interface TenderSummaryRule {
    fun check(item: TenderSummary): MutableList<String>
}

data class TenderSummaryDepotLengthRule(var maxLength: Int) : TenderSummaryRule {
    override fun check(item: TenderSummary): MutableList<String> {
        val size = item.depot.length
        if (size > maxLength) {
            return mutableListOf<String>("depot is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TenderSummaryRequiredCodeRule(var message: String) : TenderSummaryRule {
    override fun check(item: TenderSummary): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TenderSummaryCodePrefixRule(var prefix: String) : TenderSummaryRule {
    override fun check(item: TenderSummary): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TenderSummaryRequiredTitleRule(var message: String) : TenderSummaryRule {
    override fun check(item: TenderSummary): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TenderSummaryCodeLengthRule(var maxLength: Int) : TenderSummaryRule {
    override fun check(item: TenderSummary): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tender summary. */
data class TenderSummaryValidator(var rules: MutableList<TenderSummaryRule>) {
    fun validate(item: TenderSummary): MutableList<String> {
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

    fun requireValid(item: TenderSummary) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TenderSummary>): MutableMap<String, MutableList<String>> {
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

fun defaultTenderSummaryValidator(): TenderSummaryValidator {
    return TenderSummaryValidator(
        mutableListOf<TenderSummaryRule>(
            TenderSummaryDepotLengthRule(255),
            TenderSummaryRequiredCodeRule("tender summary needs an entry"),
            TenderSummaryCodePrefixRule("X"),
            TenderSummaryRequiredTitleRule("tender summary needs an entry"),
            TenderSummaryCodeLengthRule(64),
        ),
    )
}
