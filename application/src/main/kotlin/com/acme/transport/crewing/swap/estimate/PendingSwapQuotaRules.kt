package com.acme.transport.crewing.swap.estimate

/** A rule a pending swap quota must pass. Check returns its problems, or none. */
interface PendingSwapQuotaRule {
    fun check(item: PendingSwapQuota): MutableList<String>
}

data class PendingSwapQuotaDiscountRangeRule(var min: Double, var max: Double) : PendingSwapQuotaRule {
    override fun check(item: PendingSwapQuota): MutableList<String> {
        val value = item.discount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("discount $value is below $min")
        }
        if (value > max) {
            problems.add("discount $value is above $max")
        }
        return problems
    }
}

data class PendingSwapQuotaRequiredLabelRule(var message: String) : PendingSwapQuotaRule {
    override fun check(item: PendingSwapQuota): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingSwapQuotaRequiredOwnerIdRule(var message: String) : PendingSwapQuotaRule {
    override fun check(item: PendingSwapQuota): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingSwapQuotaRequiredContactEmailRule(var message: String) : PendingSwapQuotaRule {
    override fun check(item: PendingSwapQuota): MutableList<String> {
        if (item.contactEmail.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingSwapQuotaBatchSizeRangeRule(var min: Int, var max: Int) : PendingSwapQuotaRule {
    override fun check(item: PendingSwapQuota): MutableList<String> {
        val value = item.batchSize
        if (value < min || value > max) {
            return mutableListOf<String>("batch size $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending swap quota. */
data class PendingSwapQuotaValidator(var rules: MutableList<PendingSwapQuotaRule>) {
    fun validate(item: PendingSwapQuota): MutableList<String> {
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

    fun requireValid(item: PendingSwapQuota) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingSwapQuota>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingSwapQuotaValidator(): PendingSwapQuotaValidator {
    return PendingSwapQuotaValidator(
        mutableListOf<PendingSwapQuotaRule>(
            PendingSwapQuotaDiscountRangeRule(0.5, 1325.9),
            PendingSwapQuotaRequiredLabelRule("pending swap quota needs a value"),
            PendingSwapQuotaRequiredOwnerIdRule("pending swap quota needs a value"),
            PendingSwapQuotaRequiredContactEmailRule("pending swap quota needs a value"),
            PendingSwapQuotaBatchSizeRangeRule(255, 86400),
        ),
    )
}
