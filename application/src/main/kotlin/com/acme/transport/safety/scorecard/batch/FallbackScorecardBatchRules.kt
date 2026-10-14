package com.acme.transport.safety.scorecard.batch

/** A rule a fallback scorecard batch must pass. Check returns its problems, or none. */
interface FallbackScorecardBatchRule {
    fun check(item: FallbackScorecardBatch): MutableList<String>
}

data class FallbackScorecardBatchRequiredShiftCodeRule(var message: String) : FallbackScorecardBatchRule {
    override fun check(item: FallbackScorecardBatch): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackScorecardBatchRequiredLabelRule(var message: String) : FallbackScorecardBatchRule {
    override fun check(item: FallbackScorecardBatch): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackScorecardBatchCodeLengthRule(var maxLength: Int) : FallbackScorecardBatchRule {
    override fun check(item: FallbackScorecardBatch): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackScorecardBatchDriverIdLengthRule(var maxLength: Int) : FallbackScorecardBatchRule {
    override fun check(item: FallbackScorecardBatch): MutableList<String> {
        val size = item.driverId.length
        if (size > maxLength) {
            return mutableListOf<String>("driver id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback scorecard batch. */
data class FallbackScorecardBatchValidator(var rules: MutableList<FallbackScorecardBatchRule>) {
    fun validate(item: FallbackScorecardBatch): MutableList<String> {
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

    fun requireValid(item: FallbackScorecardBatch) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackScorecardBatch>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackScorecardBatchValidator(): FallbackScorecardBatchValidator {
    return FallbackScorecardBatchValidator(
        mutableListOf<FallbackScorecardBatchRule>(
            FallbackScorecardBatchRequiredShiftCodeRule("fallback scorecard batch needs an entry"),
            FallbackScorecardBatchRequiredLabelRule("fallback scorecard batch needs an entry"),
            FallbackScorecardBatchCodeLengthRule(80),
            FallbackScorecardBatchDriverIdLengthRule(3600),
        ),
    )
}
