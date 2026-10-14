package com.acme.transport.linehaul.backhaul.backhaulbatch

/** A rule a manual backhaul batch must pass. Check returns its problems, or none. */
interface ManualBackhaulBatchRule {
    fun check(item: ManualBackhaulBatch): MutableList<String>
}

data class ManualBackhaulBatchExternalRefLengthRule(var maxLength: Int) : ManualBackhaulBatchRule {
    override fun check(item: ManualBackhaulBatch): MutableList<String> {
        val size = item.externalRef.length
        if (size > maxLength) {
            return mutableListOf<String>("external ref is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualBackhaulBatchEndsAtRangeRule(var min: Int, var max: Int) : ManualBackhaulBatchRule {
    override fun check(item: ManualBackhaulBatch): MutableList<String> {
        val value = item.endsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ends at $value is below $min")
        }
        if (value > max) {
            problems.add("ends at $value is above $max")
        }
        return problems
    }
}

data class ManualBackhaulBatchLabelLengthRule(var maxLength: Int) : ManualBackhaulBatchRule {
    override fun check(item: ManualBackhaulBatch): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualBackhaulBatchRequiredExternalRefRule(var message: String) : ManualBackhaulBatchRule {
    override fun check(item: ManualBackhaulBatch): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual backhaul batch. */
data class ManualBackhaulBatchValidator(var rules: MutableList<ManualBackhaulBatchRule>) {
    fun validate(item: ManualBackhaulBatch): MutableList<String> {
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

    fun requireValid(item: ManualBackhaulBatch) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualBackhaulBatch>): MutableMap<String, MutableList<String>> {
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

fun defaultManualBackhaulBatchValidator(): ManualBackhaulBatchValidator {
    return ManualBackhaulBatchValidator(
        mutableListOf<ManualBackhaulBatchRule>(
            ManualBackhaulBatchExternalRefLengthRule(80),
            ManualBackhaulBatchEndsAtRangeRule(80, 86400),
            ManualBackhaulBatchLabelLengthRule(3600),
            ManualBackhaulBatchRequiredExternalRefRule("manual backhaul batch needs an entry"),
        ),
    )
}
