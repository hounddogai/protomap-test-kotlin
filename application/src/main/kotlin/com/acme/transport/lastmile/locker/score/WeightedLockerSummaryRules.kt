package com.acme.transport.lastmile.locker.score

/** A rule a weighted locker summary must pass. Check returns its problems, or none. */
interface WeightedLockerSummaryRule {
    fun check(item: WeightedLockerSummary): MutableList<String>
}

data class WeightedLockerSummaryCodePrefixRule(var prefix: String) : WeightedLockerSummaryRule {
    override fun check(item: WeightedLockerSummary): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class WeightedLockerSummaryChecksumLengthRule(var maxLength: Int) : WeightedLockerSummaryRule {
    override fun check(item: WeightedLockerSummary): MutableList<String> {
        val size = item.checksum.length
        if (size > maxLength) {
            return mutableListOf<String>("checksum is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedLockerSummaryRequiredTitleRule(var message: String) : WeightedLockerSummaryRule {
    override fun check(item: WeightedLockerSummary): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted locker summary. */
data class WeightedLockerSummaryValidator(var rules: MutableList<WeightedLockerSummaryRule>) {
    fun validate(item: WeightedLockerSummary): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: WeightedLockerSummary) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedLockerSummary>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedLockerSummaryValidator(): WeightedLockerSummaryValidator {
    return WeightedLockerSummaryValidator(
        mutableListOf<WeightedLockerSummaryRule>(
            WeightedLockerSummaryCodePrefixRule("Q"),
            WeightedLockerSummaryChecksumLengthRule(3600),
            WeightedLockerSummaryRequiredTitleRule("weighted locker summary needs this field"),
        ),
    )
}
