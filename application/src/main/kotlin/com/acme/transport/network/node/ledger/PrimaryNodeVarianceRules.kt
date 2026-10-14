package com.acme.transport.network.node.ledger

/** A rule a primary node variance must pass. Check returns its problems, or none. */
interface PrimaryNodeVarianceRule {
    fun check(item: PrimaryNodeVariance): MutableList<String>
}

data class PrimaryNodeVarianceTitleLengthRule(var maxLength: Int) : PrimaryNodeVarianceRule {
    override fun check(item: PrimaryNodeVariance): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryNodeVarianceCodeLengthRule(var maxLength: Int) : PrimaryNodeVarianceRule {
    override fun check(item: PrimaryNodeVariance): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryNodeVarianceRequiredDescriptionRule(var message: String) : PrimaryNodeVarianceRule {
    override fun check(item: PrimaryNodeVariance): MutableList<String> {
        if (item.description.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary node variance. */
data class PrimaryNodeVarianceValidator(var rules: MutableList<PrimaryNodeVarianceRule>) {
    fun validate(item: PrimaryNodeVariance): MutableList<String> {
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

    fun requireValid(item: PrimaryNodeVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryNodeVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryNodeVarianceValidator(): PrimaryNodeVarianceValidator {
    return PrimaryNodeVarianceValidator(
        mutableListOf<PrimaryNodeVarianceRule>(
            PrimaryNodeVarianceTitleLengthRule(3600),
            PrimaryNodeVarianceCodeLengthRule(86400),
            PrimaryNodeVarianceRequiredDescriptionRule("primary node variance needs a value"),
        ),
    )
}
