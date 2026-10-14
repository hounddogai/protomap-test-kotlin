package com.acme.transport.safety.scorecard.defaultscorecardledger

/** A rule a adaptive scorecard ledger must pass. Check returns its problems, or none. */
interface AdaptiveScorecardLedgerRule {
    fun check(item: AdaptiveScorecardLedger): MutableList<String>
}

data class AdaptiveScorecardLedgerOwnerIdLengthRule(var maxLength: Int) : AdaptiveScorecardLedgerRule {
    override fun check(item: AdaptiveScorecardLedger): MutableList<String> {
        val size = item.ownerId.length
        if (size > maxLength) {
            return mutableListOf<String>("owner id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveScorecardLedgerRequiredCodeRule(var message: String) : AdaptiveScorecardLedgerRule {
    override fun check(item: AdaptiveScorecardLedger): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveScorecardLedgerRequiredReviewerRule(var message: String) : AdaptiveScorecardLedgerRule {
    override fun check(item: AdaptiveScorecardLedger): MutableList<String> {
        if (item.reviewer.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveScorecardLedgerRequiredOwnerIdRule(var message: String) : AdaptiveScorecardLedgerRule {
    override fun check(item: AdaptiveScorecardLedger): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive scorecard ledger. */
data class AdaptiveScorecardLedgerValidator(var rules: MutableList<AdaptiveScorecardLedgerRule>) {
    fun validate(item: AdaptiveScorecardLedger): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AdaptiveScorecardLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveScorecardLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveScorecardLedgerValidator(): AdaptiveScorecardLedgerValidator {
    return AdaptiveScorecardLedgerValidator(
        mutableListOf<AdaptiveScorecardLedgerRule>(
            AdaptiveScorecardLedgerOwnerIdLengthRule(80),
            AdaptiveScorecardLedgerRequiredCodeRule("adaptive scorecard ledger needs this field"),
            AdaptiveScorecardLedgerRequiredReviewerRule("adaptive scorecard ledger needs this field"),
            AdaptiveScorecardLedgerRequiredOwnerIdRule("adaptive scorecard ledger needs an entry"),
        ),
    )
}
