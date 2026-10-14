package com.acme.transport.safety.incident.slot

/** A rule a batched incident override must pass. Check returns its problems, or none. */
interface BatchedIncidentOverrideRule {
    fun check(item: BatchedIncidentOverride): MutableList<String>
}

data class BatchedIncidentOverrideCodePrefixRule(var prefix: String) : BatchedIncidentOverrideRule {
    override fun check(item: BatchedIncidentOverride): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class BatchedIncidentOverrideDockLengthRule(var maxLength: Int) : BatchedIncidentOverrideRule {
    override fun check(item: BatchedIncidentOverride): MutableList<String> {
        val size = item.dock.length
        if (size > maxLength) {
            return mutableListOf<String>("dock is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedIncidentOverrideRequiredCurrencyRule(var message: String) : BatchedIncidentOverrideRule {
    override fun check(item: BatchedIncidentOverride): MutableList<String> {
        if (item.currency.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched incident override. */
data class BatchedIncidentOverrideValidator(var rules: MutableList<BatchedIncidentOverrideRule>) {
    fun validate(item: BatchedIncidentOverride): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BatchedIncidentOverride) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedIncidentOverride>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedIncidentOverrideValidator(): BatchedIncidentOverrideValidator {
    return BatchedIncidentOverrideValidator(
        mutableListOf<BatchedIncidentOverrideRule>(
            BatchedIncidentOverrideCodePrefixRule("OPS"),
            BatchedIncidentOverrideDockLengthRule(80),
            BatchedIncidentOverrideRequiredCurrencyRule("batched incident override needs this field"),
        ),
    )
}
