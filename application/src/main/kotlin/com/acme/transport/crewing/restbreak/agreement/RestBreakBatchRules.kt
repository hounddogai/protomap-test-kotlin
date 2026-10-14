package com.acme.transport.crewing.restbreak.agreement

/** A rule a rest break batch must pass. Check returns its problems, or none. */
interface RestBreakBatchRule {
    fun check(item: RestBreakBatch): MutableList<String>
}

data class RestBreakBatchRequiredExternalRefRule(var message: String) : RestBreakBatchRule {
    override fun check(item: RestBreakBatch): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RestBreakBatchRequiredLabelRule(var message: String) : RestBreakBatchRule {
    override fun check(item: RestBreakBatch): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RestBreakBatchExternalRefLengthRule(var maxLength: Int) : RestBreakBatchRule {
    override fun check(item: RestBreakBatch): MutableList<String> {
        val size = item.externalRef.length
        if (size > maxLength) {
            return mutableListOf<String>("external ref is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rest break batch. */
data class RestBreakBatchValidator(var rules: MutableList<RestBreakBatchRule>) {
    fun validate(item: RestBreakBatch): MutableList<String> {
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

    fun requireValid(item: RestBreakBatch) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RestBreakBatch>): MutableMap<String, MutableList<String>> {
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

fun defaultRestBreakBatchValidator(): RestBreakBatchValidator {
    return RestBreakBatchValidator(
        mutableListOf<RestBreakBatchRule>(
            RestBreakBatchRequiredExternalRefRule("rest break batch needs a value"),
            RestBreakBatchRequiredLabelRule("rest break batch needs a value"),
            RestBreakBatchExternalRefLengthRule(16),
        ),
    )
}
