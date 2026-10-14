package com.acme.transport.linehaul.tender.tenderestimate

/** A rule a strict tender estimate must pass. Check returns its problems, or none. */
interface StrictTenderEstimateRule {
    fun check(item: StrictTenderEstimate): MutableList<String>
}

data class StrictTenderEstimateTitleLengthRule(var maxLength: Int) : StrictTenderEstimateRule {
    override fun check(item: StrictTenderEstimate): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictTenderEstimateRequiredDescriptionRule(var message: String) : StrictTenderEstimateRule {
    override fun check(item: StrictTenderEstimate): MutableList<String> {
        if (item.description.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictTenderEstimateCodeLengthRule(var maxLength: Int) : StrictTenderEstimateRule {
    override fun check(item: StrictTenderEstimate): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictTenderEstimateCodePrefixRule(var prefix: String) : StrictTenderEstimateRule {
    override fun check(item: StrictTenderEstimate): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict tender estimate. */
data class StrictTenderEstimateValidator(var rules: MutableList<StrictTenderEstimateRule>) {
    fun validate(item: StrictTenderEstimate): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: StrictTenderEstimate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictTenderEstimate>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictTenderEstimateValidator(): StrictTenderEstimateValidator {
    return StrictTenderEstimateValidator(
        mutableListOf<StrictTenderEstimateRule>(
            StrictTenderEstimateTitleLengthRule(80),
            StrictTenderEstimateRequiredDescriptionRule("strict tender estimate needs an entry"),
            StrictTenderEstimateCodeLengthRule(32),
            StrictTenderEstimateCodePrefixRule("X"),
        ),
    )
}
