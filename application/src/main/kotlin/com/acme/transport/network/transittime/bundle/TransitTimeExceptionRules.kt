package com.acme.transport.network.transittime.bundle

/** A rule a transit time exception must pass. Check returns its problems, or none. */
interface TransitTimeExceptionRule {
    fun check(item: TransitTimeException): MutableList<String>
}

data class TransitTimeExceptionCodeLengthRule(var maxLength: Int) : TransitTimeExceptionRule {
    override fun check(item: TransitTimeException): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TransitTimeExceptionLabelLengthRule(var maxLength: Int) : TransitTimeExceptionRule {
    override fun check(item: TransitTimeException): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TransitTimeExceptionRequiredCurrencyRule(var message: String) : TransitTimeExceptionRule {
    override fun check(item: TransitTimeException): MutableList<String> {
        if (item.currency.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TransitTimeExceptionCarrierLengthRule(var maxLength: Int) : TransitTimeExceptionRule {
    override fun check(item: TransitTimeException): MutableList<String> {
        val size = item.carrier.length
        if (size > maxLength) {
            return mutableListOf<String>("carrier is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TransitTimeExceptionRequiredLabelRule(var message: String) : TransitTimeExceptionRule {
    override fun check(item: TransitTimeException): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TransitTimeExceptionApprovedGuardRule(var limit: Int) : TransitTimeExceptionRule {
    override fun check(item: TransitTimeException): MutableList<String> {
        if (item.approved && item.batchSize > limit) {
            return mutableListOf<String>("batch size ${item.batchSize} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a transit time exception. */
data class TransitTimeExceptionValidator(var rules: MutableList<TransitTimeExceptionRule>) {
    fun validate(item: TransitTimeException): MutableList<String> {
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

    fun requireValid(item: TransitTimeException) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TransitTimeException>): MutableMap<String, MutableList<String>> {
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

fun defaultTransitTimeExceptionValidator(): TransitTimeExceptionValidator {
    return TransitTimeExceptionValidator(
        mutableListOf<TransitTimeExceptionRule>(
            TransitTimeExceptionCodeLengthRule(1000),
            TransitTimeExceptionLabelLengthRule(1000),
            TransitTimeExceptionRequiredCurrencyRule("transit time exception needs a value"),
            TransitTimeExceptionCarrierLengthRule(1000),
            TransitTimeExceptionRequiredLabelRule("transit time exception needs this field"),
            TransitTimeExceptionApprovedGuardRule(3600),
        ),
    )
}
