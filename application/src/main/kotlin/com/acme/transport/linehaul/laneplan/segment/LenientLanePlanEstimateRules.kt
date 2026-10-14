package com.acme.transport.linehaul.laneplan.segment

/** A rule a lenient lane plan estimate must pass. Check returns its problems, or none. */
interface LenientLanePlanEstimateRule {
    fun check(item: LenientLanePlanEstimate): MutableList<String>
}

data class LenientLanePlanEstimateCodeLengthRule(var maxLength: Int) : LenientLanePlanEstimateRule {
    override fun check(item: LenientLanePlanEstimate): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientLanePlanEstimateRequiredCodeRule(var message: String) : LenientLanePlanEstimateRule {
    override fun check(item: LenientLanePlanEstimate): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientLanePlanEstimateCodePrefixRule(var prefix: String) : LenientLanePlanEstimateRule {
    override fun check(item: LenientLanePlanEstimate): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LenientLanePlanEstimateRequiredTitleRule(var message: String) : LenientLanePlanEstimateRule {
    override fun check(item: LenientLanePlanEstimate): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient lane plan estimate. */
data class LenientLanePlanEstimateValidator(var rules: MutableList<LenientLanePlanEstimateRule>) {
    fun validate(item: LenientLanePlanEstimate): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LenientLanePlanEstimate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientLanePlanEstimate>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientLanePlanEstimateValidator(): LenientLanePlanEstimateValidator {
    return LenientLanePlanEstimateValidator(
        mutableListOf<LenientLanePlanEstimateRule>(
            LenientLanePlanEstimateCodeLengthRule(16),
            LenientLanePlanEstimateRequiredCodeRule("lenient lane plan estimate needs an entry"),
            LenientLanePlanEstimateCodePrefixRule("Q"),
            LenientLanePlanEstimateRequiredTitleRule("lenient lane plan estimate needs an entry"),
        ),
    )
}
