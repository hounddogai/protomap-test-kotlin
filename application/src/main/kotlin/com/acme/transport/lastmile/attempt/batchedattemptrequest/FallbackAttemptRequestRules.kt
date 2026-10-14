package com.acme.transport.lastmile.attempt.batchedattemptrequest

/** A rule a fallback attempt request must pass. Check returns its problems, or none. */
interface FallbackAttemptRequestRule {
    fun check(item: FallbackAttemptRequest): MutableList<String>
}

data class FallbackAttemptRequestRequiredCodeRule(var message: String) : FallbackAttemptRequestRule {
    override fun check(item: FallbackAttemptRequest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackAttemptRequestLabelLengthRule(var maxLength: Int) : FallbackAttemptRequestRule {
    override fun check(item: FallbackAttemptRequest): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackAttemptRequestPhoneLengthRule(var maxLength: Int) : FallbackAttemptRequestRule {
    override fun check(item: FallbackAttemptRequest): MutableList<String> {
        val size = item.phone.length
        if (size > maxLength) {
            return mutableListOf<String>("phone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback attempt request. */
data class FallbackAttemptRequestValidator(var rules: MutableList<FallbackAttemptRequestRule>) {
    fun validate(item: FallbackAttemptRequest): MutableList<String> {
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

    fun requireValid(item: FallbackAttemptRequest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackAttemptRequest>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackAttemptRequestValidator(): FallbackAttemptRequestValidator {
    return FallbackAttemptRequestValidator(
        mutableListOf<FallbackAttemptRequestRule>(
            FallbackAttemptRequestRequiredCodeRule("fallback attempt request needs a value"),
            FallbackAttemptRequestLabelLengthRule(86400),
            FallbackAttemptRequestPhoneLengthRule(255),
        ),
    )
}
