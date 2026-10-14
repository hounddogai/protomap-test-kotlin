package com.acme.transport.safety.nearmiss.rollingmissrecord

/** A rule a adaptive near miss exception must pass. Check returns its problems, or none. */
interface AdaptiveNearMissExceptionRule {
    fun check(item: AdaptiveNearMissException): MutableList<String>
}

data class AdaptiveNearMissExceptionNameLengthRule(var maxLength: Int) : AdaptiveNearMissExceptionRule {
    override fun check(item: AdaptiveNearMissException): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveNearMissExceptionRequiredCodeRule(var message: String) : AdaptiveNearMissExceptionRule {
    override fun check(item: AdaptiveNearMissException): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveNearMissExceptionReviewerLengthRule(var maxLength: Int) : AdaptiveNearMissExceptionRule {
    override fun check(item: AdaptiveNearMissException): MutableList<String> {
        val size = item.reviewer.length
        if (size > maxLength) {
            return mutableListOf<String>("reviewer is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveNearMissExceptionCodePrefixRule(var prefix: String) : AdaptiveNearMissExceptionRule {
    override fun check(item: AdaptiveNearMissException): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive near miss exception. */
data class AdaptiveNearMissExceptionValidator(var rules: MutableList<AdaptiveNearMissExceptionRule>) {
    fun validate(item: AdaptiveNearMissException): MutableList<String> {
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

    fun requireValid(item: AdaptiveNearMissException) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveNearMissException>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveNearMissExceptionValidator(): AdaptiveNearMissExceptionValidator {
    return AdaptiveNearMissExceptionValidator(
        mutableListOf<AdaptiveNearMissExceptionRule>(
            AdaptiveNearMissExceptionNameLengthRule(3600),
            AdaptiveNearMissExceptionRequiredCodeRule("adaptive near miss exception needs this field"),
            AdaptiveNearMissExceptionReviewerLengthRule(64),
            AdaptiveNearMissExceptionCodePrefixRule("REF"),
        ),
    )
}
