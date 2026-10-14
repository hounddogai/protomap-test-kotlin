package com.acme.transport.freightpay.audit.recorditem

/** A rule a global audit adjustment must pass. Check returns its problems, or none. */
interface GlobalAuditAdjustmentRule {
    fun check(item: GlobalAuditAdjustment): MutableList<String>
}

data class GlobalAuditAdjustmentEtaMinutesRangeRule(var min: Int, var max: Int) : GlobalAuditAdjustmentRule {
    override fun check(item: GlobalAuditAdjustment): MutableList<String> {
        val value = item.etaMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("eta minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class GlobalAuditAdjustmentLoadKgRangeRule(var min: Double, var max: Double) : GlobalAuditAdjustmentRule {
    override fun check(item: GlobalAuditAdjustment): MutableList<String> {
        val value = item.loadKg
        if (value < min || value > max) {
            return mutableListOf<String>("load kg $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class GlobalAuditAdjustmentCodeLengthRule(var maxLength: Int) : GlobalAuditAdjustmentRule {
    override fun check(item: GlobalAuditAdjustment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalAuditAdjustmentRequiredCurrencyRule(var message: String) : GlobalAuditAdjustmentRule {
    override fun check(item: GlobalAuditAdjustment): MutableList<String> {
        if (item.currency.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalAuditAdjustmentRequiredTitleRule(var message: String) : GlobalAuditAdjustmentRule {
    override fun check(item: GlobalAuditAdjustment): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global audit adjustment. */
data class GlobalAuditAdjustmentValidator(var rules: MutableList<GlobalAuditAdjustmentRule>) {
    fun validate(item: GlobalAuditAdjustment): MutableList<String> {
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

    fun requireValid(item: GlobalAuditAdjustment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalAuditAdjustment>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalAuditAdjustmentValidator(): GlobalAuditAdjustmentValidator {
    return GlobalAuditAdjustmentValidator(
        mutableListOf<GlobalAuditAdjustmentRule>(
            GlobalAuditAdjustmentEtaMinutesRangeRule(64, 86400),
            GlobalAuditAdjustmentLoadKgRangeRule(5.9, 2635.4),
            GlobalAuditAdjustmentCodeLengthRule(3600),
            GlobalAuditAdjustmentRequiredCurrencyRule("global audit adjustment needs an entry"),
            GlobalAuditAdjustmentRequiredTitleRule("global audit adjustment needs this field"),
        ),
    )
}
