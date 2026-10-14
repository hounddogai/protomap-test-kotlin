package com.acme.transport.fleetops.tire.request

/** A rule a adaptive tire exception must pass. Check returns its problems, or none. */
interface AdaptiveTireExceptionRule {
    fun check(item: AdaptiveTireException): MutableList<String>
}

data class AdaptiveTireExceptionArchivedGuardRule(var limit: Double) : AdaptiveTireExceptionRule {
    override fun check(item: AdaptiveTireException): MutableList<String> {
        if (item.archived && item.amount > limit) {
            return mutableListOf<String>("amount ${item.amount} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveTireExceptionRequiredCodeRule(var message: String) : AdaptiveTireExceptionRule {
    override fun check(item: AdaptiveTireException): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveTireExceptionCodePrefixRule(var prefix: String) : AdaptiveTireExceptionRule {
    override fun check(item: AdaptiveTireException): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveTireExceptionRequiredSegmentRule(var message: String) : AdaptiveTireExceptionRule {
    override fun check(item: AdaptiveTireException): MutableList<String> {
        if (item.segment.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveTireExceptionBatchSizeRangeRule(var min: Int, var max: Int) : AdaptiveTireExceptionRule {
    override fun check(item: AdaptiveTireException): MutableList<String> {
        val value = item.batchSize
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("batch size $value is below $min")
        }
        if (value > max) {
            problems.add("batch size $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a adaptive tire exception. */
data class AdaptiveTireExceptionValidator(var rules: MutableList<AdaptiveTireExceptionRule>) {
    fun validate(item: AdaptiveTireException): MutableList<String> {
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

    fun requireValid(item: AdaptiveTireException) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveTireException>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveTireExceptionValidator(): AdaptiveTireExceptionValidator {
    return AdaptiveTireExceptionValidator(
        mutableListOf<AdaptiveTireExceptionRule>(
            AdaptiveTireExceptionArchivedGuardRule(5.1),
            AdaptiveTireExceptionRequiredCodeRule("adaptive tire exception needs a value"),
            AdaptiveTireExceptionCodePrefixRule("Q"),
            AdaptiveTireExceptionRequiredSegmentRule("adaptive tire exception needs this field"),
            AdaptiveTireExceptionBatchSizeRangeRule(3600, 3600),
        ),
    )
}
