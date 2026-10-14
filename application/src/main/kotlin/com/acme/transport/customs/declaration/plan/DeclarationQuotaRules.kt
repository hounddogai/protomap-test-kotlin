package com.acme.transport.customs.declaration.plan

/** A rule a declaration quota must pass. Check returns its problems, or none. */
interface DeclarationQuotaRule {
    fun check(item: DeclarationQuota): MutableList<String>
}

data class DeclarationQuotaRatioRangeRule(var min: Double, var max: Double) : DeclarationQuotaRule {
    override fun check(item: DeclarationQuota): MutableList<String> {
        val value = item.ratio
        if (value < min || value > max) {
            return mutableListOf<String>("ratio $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DeclarationQuotaLabelLengthRule(var maxLength: Int) : DeclarationQuotaRule {
    override fun check(item: DeclarationQuota): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DeclarationQuotaRequiredLabelRule(var message: String) : DeclarationQuotaRule {
    override fun check(item: DeclarationQuota): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DeclarationQuotaRequiredCodeRule(var message: String) : DeclarationQuotaRule {
    override fun check(item: DeclarationQuota): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DeclarationQuotaCeilingRangeRule(var min: Double, var max: Double) : DeclarationQuotaRule {
    override fun check(item: DeclarationQuota): MutableList<String> {
        val value = item.ceiling
        if (value < min || value > max) {
            return mutableListOf<String>("ceiling $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DeclarationQuotaCodeLengthRule(var maxLength: Int) : DeclarationQuotaRule {
    override fun check(item: DeclarationQuota): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a declaration quota. */
data class DeclarationQuotaValidator(var rules: MutableList<DeclarationQuotaRule>) {
    fun validate(item: DeclarationQuota): MutableList<String> {
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

    fun requireValid(item: DeclarationQuota) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DeclarationQuota>): MutableMap<String, MutableList<String>> {
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

fun defaultDeclarationQuotaValidator(): DeclarationQuotaValidator {
    return DeclarationQuotaValidator(
        mutableListOf<DeclarationQuotaRule>(
            DeclarationQuotaRatioRangeRule(1.5, 4951.4),
            DeclarationQuotaLabelLengthRule(3600),
            DeclarationQuotaRequiredLabelRule("declaration quota needs this field"),
            DeclarationQuotaRequiredCodeRule("declaration quota needs this field"),
            DeclarationQuotaCeilingRangeRule(9.2, 1465.5),
            DeclarationQuotaCodeLengthRule(16),
        ),
    )
}
