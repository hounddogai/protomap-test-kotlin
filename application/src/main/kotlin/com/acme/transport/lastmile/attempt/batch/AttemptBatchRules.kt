package com.acme.transport.lastmile.attempt.batch

/** A rule a attempt batch must pass. Check returns its problems, or none. */
interface AttemptBatchRule {
    fun check(item: AttemptBatch): MutableList<String>
}

data class AttemptBatchCodePrefixRule(var prefix: String) : AttemptBatchRule {
    override fun check(item: AttemptBatch): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class AttemptBatchCountryLengthRule(var maxLength: Int) : AttemptBatchRule {
    override fun check(item: AttemptBatch): MutableList<String> {
        val size = item.country.length
        if (size > maxLength) {
            return mutableListOf<String>("country is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AttemptBatchCodeLengthRule(var maxLength: Int) : AttemptBatchRule {
    override fun check(item: AttemptBatch): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AttemptBatchNameLengthRule(var maxLength: Int) : AttemptBatchRule {
    override fun check(item: AttemptBatch): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AttemptBatchRequiredNameRule(var message: String) : AttemptBatchRule {
    override fun check(item: AttemptBatch): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AttemptBatchRequiredCountryRule(var message: String) : AttemptBatchRule {
    override fun check(item: AttemptBatch): MutableList<String> {
        if (item.country.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a attempt batch. */
data class AttemptBatchValidator(var rules: MutableList<AttemptBatchRule>) {
    fun validate(item: AttemptBatch): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AttemptBatch) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AttemptBatch>): MutableMap<String, MutableList<String>> {
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

fun defaultAttemptBatchValidator(): AttemptBatchValidator {
    return AttemptBatchValidator(
        mutableListOf<AttemptBatchRule>(
            AttemptBatchCodePrefixRule("X"),
            AttemptBatchCountryLengthRule(64),
            AttemptBatchCodeLengthRule(32),
            AttemptBatchNameLengthRule(120),
            AttemptBatchRequiredNameRule("attempt batch needs an entry"),
            AttemptBatchRequiredCountryRule("attempt batch needs this field"),
        ),
    )
}
