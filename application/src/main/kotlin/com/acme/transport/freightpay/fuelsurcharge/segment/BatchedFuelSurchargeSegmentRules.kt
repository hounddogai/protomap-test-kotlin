package com.acme.transport.freightpay.fuelsurcharge.segment

/** A rule a batched fuel surcharge segment must pass. Check returns its problems, or none. */
interface BatchedFuelSurchargeSegmentRule {
    fun check(item: BatchedFuelSurchargeSegment): MutableList<String>
}

data class BatchedFuelSurchargeSegmentPalletsRangeRule(
    var min: Int,
    var max: Int,
) : BatchedFuelSurchargeSegmentRule {
    override fun check(item: BatchedFuelSurchargeSegment): MutableList<String> {
        val value = item.pallets
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("pallets $value is below $min")
        }
        if (value > max) {
            problems.add("pallets $value is above $max")
        }
        return problems
    }
}

data class BatchedFuelSurchargeSegmentLabelLengthRule(var maxLength: Int) : BatchedFuelSurchargeSegmentRule {
    override fun check(item: BatchedFuelSurchargeSegment): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedFuelSurchargeSegmentRequiredSummaryRule(
    var message: String,
) : BatchedFuelSurchargeSegmentRule {
    override fun check(item: BatchedFuelSurchargeSegment): MutableList<String> {
        if (item.summary.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedFuelSurchargeSegmentRequiredNameRule(var message: String) : BatchedFuelSurchargeSegmentRule {
    override fun check(item: BatchedFuelSurchargeSegment): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedFuelSurchargeSegmentRequiredLabelRule(var message: String) : BatchedFuelSurchargeSegmentRule {
    override fun check(item: BatchedFuelSurchargeSegment): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched fuel surcharge segment. */
data class BatchedFuelSurchargeSegmentValidator(var rules: MutableList<BatchedFuelSurchargeSegmentRule>) {
    fun validate(item: BatchedFuelSurchargeSegment): MutableList<String> {
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

    fun requireValid(item: BatchedFuelSurchargeSegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedFuelSurchargeSegment>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedFuelSurchargeSegmentValidator(): BatchedFuelSurchargeSegmentValidator {
    return BatchedFuelSurchargeSegmentValidator(
        mutableListOf<BatchedFuelSurchargeSegmentRule>(
            BatchedFuelSurchargeSegmentPalletsRangeRule(32, 86400),
            BatchedFuelSurchargeSegmentLabelLengthRule(3600),
            BatchedFuelSurchargeSegmentRequiredSummaryRule("batched fuel surcharge segment needs an entry"),
            BatchedFuelSurchargeSegmentRequiredNameRule("batched fuel surcharge segment needs this field"),
            BatchedFuelSurchargeSegmentRequiredLabelRule("batched fuel surcharge segment needs this field"),
        ),
    )
}
