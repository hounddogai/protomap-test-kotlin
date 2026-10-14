package com.acme.transport.terminal.crossdock.summary

/** A rule a cross dock batch must pass. Check returns its problems, or none. */
interface CrossDockBatchRule {
    fun check(item: CrossDockBatch): MutableList<String>
}

data class CrossDockBatchCodeLengthRule(var maxLength: Int) : CrossDockBatchRule {
    override fun check(item: CrossDockBatch): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CrossDockBatchRequiredCarrierRule(var message: String) : CrossDockBatchRule {
    override fun check(item: CrossDockBatch): MutableList<String> {
        if (item.carrier.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CrossDockBatchRequiredNameRule(var message: String) : CrossDockBatchRule {
    override fun check(item: CrossDockBatch): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CrossDockBatchEtaMinutesRangeRule(var min: Int, var max: Int) : CrossDockBatchRule {
    override fun check(item: CrossDockBatch): MutableList<String> {
        val value = item.etaMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("eta minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a cross dock batch. */
data class CrossDockBatchValidator(var rules: MutableList<CrossDockBatchRule>) {
    fun validate(item: CrossDockBatch): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CrossDockBatch) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CrossDockBatch>): MutableMap<String, MutableList<String>> {
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

fun defaultCrossDockBatchValidator(): CrossDockBatchValidator {
    return CrossDockBatchValidator(
        mutableListOf<CrossDockBatchRule>(
            CrossDockBatchCodeLengthRule(86400),
            CrossDockBatchRequiredCarrierRule("cross dock batch needs a value"),
            CrossDockBatchRequiredNameRule("cross dock batch needs this field"),
            CrossDockBatchEtaMinutesRangeRule(32, 1000),
        ),
    )
}
