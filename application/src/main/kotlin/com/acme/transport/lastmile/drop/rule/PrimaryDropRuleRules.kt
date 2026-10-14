package com.acme.transport.lastmile.drop.rule

/** A rule a primary drop rule must pass. Check returns its problems, or none. */
interface PrimaryDropRuleRule {
    fun check(item: PrimaryDropRule): MutableList<String>
}

data class PrimaryDropRuleUpdatedAtRangeRule(var min: Int, var max: Int) : PrimaryDropRuleRule {
    override fun check(item: PrimaryDropRule): MutableList<String> {
        val value = item.updatedAt
        if (value < min || value > max) {
            return mutableListOf<String>("updated at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryDropRuleRequiredChecksumRule(var message: String) : PrimaryDropRuleRule {
    override fun check(item: PrimaryDropRule): MutableList<String> {
        if (item.checksum.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryDropRuleChecksumLengthRule(var maxLength: Int) : PrimaryDropRuleRule {
    override fun check(item: PrimaryDropRule): MutableList<String> {
        val size = item.checksum.length
        if (size > maxLength) {
            return mutableListOf<String>("checksum is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary drop rule. */
data class PrimaryDropRuleValidator(var rules: MutableList<PrimaryDropRuleRule>) {
    fun validate(item: PrimaryDropRule): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PrimaryDropRule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryDropRule>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryDropRuleValidator(): PrimaryDropRuleValidator {
    return PrimaryDropRuleValidator(
        mutableListOf<PrimaryDropRuleRule>(
            PrimaryDropRuleUpdatedAtRangeRule(64, 80),
            PrimaryDropRuleRequiredChecksumRule("primary drop rule needs an entry"),
            PrimaryDropRuleChecksumLengthRule(80),
        ),
    )
}
