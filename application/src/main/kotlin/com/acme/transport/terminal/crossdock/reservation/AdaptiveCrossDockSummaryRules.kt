package com.acme.transport.terminal.crossdock.reservation

/** A rule a adaptive cross dock summary must pass. Check returns its problems, or none. */
interface AdaptiveCrossDockSummaryRule {
    fun check(item: AdaptiveCrossDockSummary): MutableList<String>
}

data class AdaptiveCrossDockSummaryPhoneLengthRule(var maxLength: Int) : AdaptiveCrossDockSummaryRule {
    override fun check(item: AdaptiveCrossDockSummary): MutableList<String> {
        val size = item.phone.length
        if (size > maxLength) {
            return mutableListOf<String>("phone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveCrossDockSummaryCarrierLengthRule(var maxLength: Int) : AdaptiveCrossDockSummaryRule {
    override fun check(item: AdaptiveCrossDockSummary): MutableList<String> {
        val size = item.carrier.length
        if (size > maxLength) {
            return mutableListOf<String>("carrier is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveCrossDockSummaryRequiredCodeRule(var message: String) : AdaptiveCrossDockSummaryRule {
    override fun check(item: AdaptiveCrossDockSummary): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveCrossDockSummaryCodePrefixRule(var prefix: String) : AdaptiveCrossDockSummaryRule {
    override fun check(item: AdaptiveCrossDockSummary): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive cross dock summary. */
data class AdaptiveCrossDockSummaryValidator(var rules: MutableList<AdaptiveCrossDockSummaryRule>) {
    fun validate(item: AdaptiveCrossDockSummary): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AdaptiveCrossDockSummary) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveCrossDockSummary>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveCrossDockSummaryValidator(): AdaptiveCrossDockSummaryValidator {
    return AdaptiveCrossDockSummaryValidator(
        mutableListOf<AdaptiveCrossDockSummaryRule>(
            AdaptiveCrossDockSummaryPhoneLengthRule(120),
            AdaptiveCrossDockSummaryCarrierLengthRule(1000),
            AdaptiveCrossDockSummaryRequiredCodeRule("adaptive cross dock summary needs this field"),
            AdaptiveCrossDockSummaryCodePrefixRule("REF"),
        ),
    )
}
