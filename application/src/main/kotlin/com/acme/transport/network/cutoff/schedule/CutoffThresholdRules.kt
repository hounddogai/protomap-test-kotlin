package com.acme.transport.network.cutoff.schedule

/** A rule a cutoff threshold must pass. Check returns its problems, or none. */
interface CutoffThresholdRule {
    fun check(item: CutoffThreshold): MutableList<String>
}

data class CutoffThresholdRequiredExternalRefRule(var message: String) : CutoffThresholdRule {
    override fun check(item: CutoffThreshold): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CutoffThresholdRequiredCodeRule(var message: String) : CutoffThresholdRule {
    override fun check(item: CutoffThreshold): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CutoffThresholdCodeLengthRule(var maxLength: Int) : CutoffThresholdRule {
    override fun check(item: CutoffThreshold): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CutoffThresholdThresholdRangeRule(var min: Double, var max: Double) : CutoffThresholdRule {
    override fun check(item: CutoffThreshold): MutableList<String> {
        val value = item.threshold
        if (value < min || value > max) {
            return mutableListOf<String>("threshold $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CutoffThresholdPriorityRangeRule(var min: Int, var max: Int) : CutoffThresholdRule {
    override fun check(item: CutoffThreshold): MutableList<String> {
        val value = item.priority
        if (value < min || value > max) {
            return mutableListOf<String>("priority $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a cutoff threshold. */
data class CutoffThresholdValidator(var rules: MutableList<CutoffThresholdRule>) {
    fun validate(item: CutoffThreshold): MutableList<String> {
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

    fun requireValid(item: CutoffThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CutoffThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultCutoffThresholdValidator(): CutoffThresholdValidator {
    return CutoffThresholdValidator(
        mutableListOf<CutoffThresholdRule>(
            CutoffThresholdRequiredExternalRefRule("cutoff threshold needs a value"),
            CutoffThresholdRequiredCodeRule("cutoff threshold needs an entry"),
            CutoffThresholdCodeLengthRule(80),
            CutoffThresholdThresholdRangeRule(8.0, 2232.6),
            CutoffThresholdPriorityRangeRule(16, 86400),
        ),
    )
}
