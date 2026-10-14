package com.acme.transport.network.cutoff.batch

/** A rule a cutoff batch must pass. Check returns its problems, or none. */
interface CutoffBatchRule {
    fun check(item: CutoffBatch): MutableList<String>
}

data class CutoffBatchMinimumRangeRule(var min: Double, var max: Double) : CutoffBatchRule {
    override fun check(item: CutoffBatch): MutableList<String> {
        val value = item.minimum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("minimum $value is below $min")
        }
        if (value > max) {
            problems.add("minimum $value is above $max")
        }
        return problems
    }
}

data class CutoffBatchRequiredCodeRule(var message: String) : CutoffBatchRule {
    override fun check(item: CutoffBatch): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CutoffBatchEtaMinutesRangeRule(var min: Int, var max: Int) : CutoffBatchRule {
    override fun check(item: CutoffBatch): MutableList<String> {
        val value = item.etaMinutes
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("eta minutes $value is below $min")
        }
        if (value > max) {
            problems.add("eta minutes $value is above $max")
        }
        return problems
    }
}

data class CutoffBatchCodePrefixRule(var prefix: String) : CutoffBatchRule {
    override fun check(item: CutoffBatch): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class CutoffBatchRequiredDockRule(var message: String) : CutoffBatchRule {
    override fun check(item: CutoffBatch): MutableList<String> {
        if (item.dock.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a cutoff batch. */
data class CutoffBatchValidator(var rules: MutableList<CutoffBatchRule>) {
    fun validate(item: CutoffBatch): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CutoffBatch) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CutoffBatch>): MutableMap<String, MutableList<String>> {
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

fun defaultCutoffBatchValidator(): CutoffBatchValidator {
    return CutoffBatchValidator(
        mutableListOf<CutoffBatchRule>(
            CutoffBatchMinimumRangeRule(5.8, 2089.4),
            CutoffBatchRequiredCodeRule("cutoff batch needs an entry"),
            CutoffBatchEtaMinutesRangeRule(16, 80),
            CutoffBatchCodePrefixRule("ACM"),
            CutoffBatchRequiredDockRule("cutoff batch needs a value"),
        ),
    )
}
