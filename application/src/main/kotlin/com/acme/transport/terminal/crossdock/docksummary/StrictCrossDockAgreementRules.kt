package com.acme.transport.terminal.crossdock.docksummary

import com.acme.transport.kit.text.normalize

/** A rule a strict cross dock agreement must pass. Check returns its problems, or none. */
interface StrictCrossDockAgreementRule {
    fun check(item: StrictCrossDockAgreement): MutableList<String>
}

data class StrictCrossDockAgreementRequiredCodeRule(var message: String) : StrictCrossDockAgreementRule {
    override fun check(item: StrictCrossDockAgreement): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictCrossDockAgreementAllowedCurrencyRule(
    var allowed: MutableList<String>,
) : StrictCrossDockAgreementRule {
    override fun check(item: StrictCrossDockAgreement): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.currency))) {
            return mutableListOf<String>("currency ${item.currency} is too long")
        }
        return mutableListOf<String>()
    }
}

data class StrictCrossDockAgreementRequiredLaneRule(var message: String) : StrictCrossDockAgreementRule {
    override fun check(item: StrictCrossDockAgreement): MutableList<String> {
        if (item.lane.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictCrossDockAgreementCodeLengthRule(var maxLength: Int) : StrictCrossDockAgreementRule {
    override fun check(item: StrictCrossDockAgreement): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict cross dock agreement. */
data class StrictCrossDockAgreementValidator(var rules: MutableList<StrictCrossDockAgreementRule>) {
    fun validate(item: StrictCrossDockAgreement): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: StrictCrossDockAgreement) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictCrossDockAgreement>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictCrossDockAgreementValidator(): StrictCrossDockAgreementValidator {
    return StrictCrossDockAgreementValidator(
        mutableListOf<StrictCrossDockAgreementRule>(
            StrictCrossDockAgreementRequiredCodeRule("strict cross dock agreement needs an entry"),
            StrictCrossDockAgreementAllowedCurrencyRule(mutableListOf<String>()),
            StrictCrossDockAgreementRequiredLaneRule("strict cross dock agreement needs a value"),
            StrictCrossDockAgreementCodeLengthRule(3600),
        ),
    )
}
