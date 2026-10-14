package com.acme.transport.network.cutoff.draft

/** A rule a cutoff metric must pass. Check returns its problems, or none. */
interface CutoffMetricRule {
    fun check(item: CutoffMetric): MutableList<String>
}

data class CutoffMetricCodePrefixRule(var prefix: String) : CutoffMetricRule {
    override fun check(item: CutoffMetric): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class CutoffMetricAttemptsRangeRule(var min: Int, var max: Int) : CutoffMetricRule {
    override fun check(item: CutoffMetric): MutableList<String> {
        val value = item.attempts
        if (value < min || value > max) {
            return mutableListOf<String>("attempts $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CutoffMetricRequiredExternalRefRule(var message: String) : CutoffMetricRule {
    override fun check(item: CutoffMetric): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CutoffMetricCodeLengthRule(var maxLength: Int) : CutoffMetricRule {
    override fun check(item: CutoffMetric): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CutoffMetricRequiredNameRule(var message: String) : CutoffMetricRule {
    override fun check(item: CutoffMetric): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a cutoff metric. */
data class CutoffMetricValidator(var rules: MutableList<CutoffMetricRule>) {
    fun validate(item: CutoffMetric): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CutoffMetric) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CutoffMetric>): MutableMap<String, MutableList<String>> {
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

fun defaultCutoffMetricValidator(): CutoffMetricValidator {
    return CutoffMetricValidator(
        mutableListOf<CutoffMetricRule>(
            CutoffMetricCodePrefixRule("X"),
            CutoffMetricAttemptsRangeRule(80, 120),
            CutoffMetricRequiredExternalRefRule("cutoff metric needs this field"),
            CutoffMetricCodeLengthRule(3600),
            CutoffMetricRequiredNameRule("cutoff metric needs an entry"),
        ),
    )
}
