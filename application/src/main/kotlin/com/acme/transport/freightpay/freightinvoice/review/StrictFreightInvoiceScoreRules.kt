package com.acme.transport.freightpay.freightinvoice.review

/** A rule a strict freight invoice score must pass. Check returns its problems, or none. */
interface StrictFreightInvoiceScoreRule {
    fun check(item: StrictFreightInvoiceScore): MutableList<String>
}

data class StrictFreightInvoiceScoreRequiredTitleRule(var message: String) : StrictFreightInvoiceScoreRule {
    override fun check(item: StrictFreightInvoiceScore): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictFreightInvoiceScoreRequiredCodeRule(var message: String) : StrictFreightInvoiceScoreRule {
    override fun check(item: StrictFreightInvoiceScore): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictFreightInvoiceScoreCodeLengthRule(var maxLength: Int) : StrictFreightInvoiceScoreRule {
    override fun check(item: StrictFreightInvoiceScore): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictFreightInvoiceScoreOwnerIdLengthRule(var maxLength: Int) : StrictFreightInvoiceScoreRule {
    override fun check(item: StrictFreightInvoiceScore): MutableList<String> {
        val size = item.ownerId.length
        if (size > maxLength) {
            return mutableListOf<String>("owner id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictFreightInvoiceScoreRegionLengthRule(var maxLength: Int) : StrictFreightInvoiceScoreRule {
    override fun check(item: StrictFreightInvoiceScore): MutableList<String> {
        val size = item.region.length
        if (size > maxLength) {
            return mutableListOf<String>("region is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict freight invoice score. */
data class StrictFreightInvoiceScoreValidator(var rules: MutableList<StrictFreightInvoiceScoreRule>) {
    fun validate(item: StrictFreightInvoiceScore): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: StrictFreightInvoiceScore) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictFreightInvoiceScore>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictFreightInvoiceScoreValidator(): StrictFreightInvoiceScoreValidator {
    return StrictFreightInvoiceScoreValidator(
        mutableListOf<StrictFreightInvoiceScoreRule>(
            StrictFreightInvoiceScoreRequiredTitleRule("strict freight invoice score needs an entry"),
            StrictFreightInvoiceScoreRequiredCodeRule("strict freight invoice score needs a value"),
            StrictFreightInvoiceScoreCodeLengthRule(32),
            StrictFreightInvoiceScoreOwnerIdLengthRule(32),
            StrictFreightInvoiceScoreRegionLengthRule(120),
        ),
    )
}
