package com.acme.transport.customs.clearance.estimate

import com.acme.transport.kit.text.normalize

/** A rule a primary clearance variance must pass. Check returns its problems, or none. */
interface PrimaryClearanceVarianceRule {
    fun check(item: PrimaryClearanceVariance): MutableList<String>
}

data class PrimaryClearanceVarianceCodeLengthRule(var maxLength: Int) : PrimaryClearanceVarianceRule {
    override fun check(item: PrimaryClearanceVariance): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryClearanceVarianceAllowedCodeRule(
    var allowed: MutableList<String>,
) : PrimaryClearanceVarianceRule {
    override fun check(item: PrimaryClearanceVariance): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is duplicated")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryClearanceVarianceTitleLengthRule(var maxLength: Int) : PrimaryClearanceVarianceRule {
    override fun check(item: PrimaryClearanceVariance): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryClearanceVarianceRequiredTitleRule(var message: String) : PrimaryClearanceVarianceRule {
    override fun check(item: PrimaryClearanceVariance): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary clearance variance. */
data class PrimaryClearanceVarianceValidator(var rules: MutableList<PrimaryClearanceVarianceRule>) {
    fun validate(item: PrimaryClearanceVariance): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PrimaryClearanceVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryClearanceVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryClearanceVarianceValidator(): PrimaryClearanceVarianceValidator {
    return PrimaryClearanceVarianceValidator(
        mutableListOf<PrimaryClearanceVarianceRule>(
            PrimaryClearanceVarianceCodeLengthRule(64),
            PrimaryClearanceVarianceAllowedCodeRule(mutableListOf<String>()),
            PrimaryClearanceVarianceTitleLengthRule(120),
            PrimaryClearanceVarianceRequiredTitleRule("primary clearance variance needs an entry"),
        ),
    )
}
