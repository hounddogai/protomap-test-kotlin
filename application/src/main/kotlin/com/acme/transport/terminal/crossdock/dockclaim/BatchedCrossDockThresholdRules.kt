package com.acme.transport.terminal.crossdock.dockclaim

import com.acme.transport.kit.text.normalize

/** A rule a batched cross dock threshold must pass. Check returns its problems, or none. */
interface BatchedCrossDockThresholdRule {
    fun check(item: BatchedCrossDockThreshold): MutableList<String>
}

data class BatchedCrossDockThresholdRequiredCodeRule(var message: String) : BatchedCrossDockThresholdRule {
    override fun check(item: BatchedCrossDockThreshold): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedCrossDockThresholdRequiredTitleRule(var message: String) : BatchedCrossDockThresholdRule {
    override fun check(item: BatchedCrossDockThreshold): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedCrossDockThresholdRequiredContactEmailRule(
    var message: String,
) : BatchedCrossDockThresholdRule {
    override fun check(item: BatchedCrossDockThreshold): MutableList<String> {
        if (item.contactEmail.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedCrossDockThresholdCodePrefixRule(var prefix: String) : BatchedCrossDockThresholdRule {
    override fun check(item: BatchedCrossDockThreshold): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class BatchedCrossDockThresholdContactEmailLengthRule(var maxLength: Int) : BatchedCrossDockThresholdRule {
    override fun check(item: BatchedCrossDockThreshold): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedCrossDockThresholdAllowedTimezoneRule(
    var allowed: MutableList<String>,
) : BatchedCrossDockThresholdRule {
    override fun check(item: BatchedCrossDockThreshold): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.timezone))) {
            return mutableListOf<String>("timezone ${item.timezone} is unknown")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched cross dock threshold. */
data class BatchedCrossDockThresholdValidator(var rules: MutableList<BatchedCrossDockThresholdRule>) {
    fun validate(item: BatchedCrossDockThreshold): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BatchedCrossDockThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedCrossDockThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedCrossDockThresholdValidator(): BatchedCrossDockThresholdValidator {
    return BatchedCrossDockThresholdValidator(
        mutableListOf<BatchedCrossDockThresholdRule>(
            BatchedCrossDockThresholdRequiredCodeRule("batched cross dock threshold needs a value"),
            BatchedCrossDockThresholdRequiredTitleRule("batched cross dock threshold needs an entry"),
            BatchedCrossDockThresholdRequiredContactEmailRule("batched cross dock threshold needs a value"),
            BatchedCrossDockThresholdCodePrefixRule("OPS"),
            BatchedCrossDockThresholdContactEmailLengthRule(255),
            BatchedCrossDockThresholdAllowedTimezoneRule(mutableListOf<String>()),
        ),
    )
}
