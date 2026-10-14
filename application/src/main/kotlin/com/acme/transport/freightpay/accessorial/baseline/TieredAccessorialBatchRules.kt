package com.acme.transport.freightpay.accessorial.baseline

/** A rule a tiered accessorial batch must pass. Check returns its problems, or none. */
interface TieredAccessorialBatchRule {
    fun check(item: TieredAccessorialBatch): MutableList<String>
}

data class TieredAccessorialBatchCodeLengthRule(var maxLength: Int) : TieredAccessorialBatchRule {
    override fun check(item: TieredAccessorialBatch): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredAccessorialBatchRequiredDockRule(var message: String) : TieredAccessorialBatchRule {
    override fun check(item: TieredAccessorialBatch): MutableList<String> {
        if (item.dock.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredAccessorialBatchLaneLengthRule(var maxLength: Int) : TieredAccessorialBatchRule {
    override fun check(item: TieredAccessorialBatch): MutableList<String> {
        val size = item.lane.length
        if (size > maxLength) {
            return mutableListOf<String>("lane is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered accessorial batch. */
data class TieredAccessorialBatchValidator(var rules: MutableList<TieredAccessorialBatchRule>) {
    fun validate(item: TieredAccessorialBatch): MutableList<String> {
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

    fun requireValid(item: TieredAccessorialBatch) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredAccessorialBatch>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredAccessorialBatchValidator(): TieredAccessorialBatchValidator {
    return TieredAccessorialBatchValidator(
        mutableListOf<TieredAccessorialBatchRule>(
            TieredAccessorialBatchCodeLengthRule(120),
            TieredAccessorialBatchRequiredDockRule("tiered accessorial batch needs a value"),
            TieredAccessorialBatchLaneLengthRule(80),
        ),
    )
}
