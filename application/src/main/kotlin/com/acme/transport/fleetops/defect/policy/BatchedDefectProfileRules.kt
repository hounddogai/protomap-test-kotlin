package com.acme.transport.fleetops.defect.policy

/** A rule a batched defect profile must pass. Check returns its problems, or none. */
interface BatchedDefectProfileRule {
    fun check(item: BatchedDefectProfile): MutableList<String>
}

data class BatchedDefectProfileRequiredExternalRefRule(var message: String) : BatchedDefectProfileRule {
    override fun check(item: BatchedDefectProfile): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedDefectProfileRequiredNameRule(var message: String) : BatchedDefectProfileRule {
    override fun check(item: BatchedDefectProfile): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedDefectProfileRequiredCodeRule(var message: String) : BatchedDefectProfileRule {
    override fun check(item: BatchedDefectProfile): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedDefectProfileEtaMinutesRangeRule(var min: Int, var max: Int) : BatchedDefectProfileRule {
    override fun check(item: BatchedDefectProfile): MutableList<String> {
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

/** Runs every rule of a batched defect profile. */
data class BatchedDefectProfileValidator(var rules: MutableList<BatchedDefectProfileRule>) {
    fun validate(item: BatchedDefectProfile): MutableList<String> {
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

    fun requireValid(item: BatchedDefectProfile) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedDefectProfile>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedDefectProfileValidator(): BatchedDefectProfileValidator {
    return BatchedDefectProfileValidator(
        mutableListOf<BatchedDefectProfileRule>(
            BatchedDefectProfileRequiredExternalRefRule("batched defect profile needs this field"),
            BatchedDefectProfileRequiredNameRule("batched defect profile needs a value"),
            BatchedDefectProfileRequiredCodeRule("batched defect profile needs an entry"),
            BatchedDefectProfileEtaMinutesRangeRule(16, 86400),
        ),
    )
}
