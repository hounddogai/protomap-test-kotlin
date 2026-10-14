package com.acme.transport.linehaul.capacity.target

/** A rule a capped capacity mapping must pass. Check returns its problems, or none. */
interface CappedCapacityMappingRule {
    fun check(item: CappedCapacityMapping): MutableList<String>
}

data class CappedCapacityMappingRequiredReviewerRule(var message: String) : CappedCapacityMappingRule {
    override fun check(item: CappedCapacityMapping): MutableList<String> {
        if (item.reviewer.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedCapacityMappingCurrencyLengthRule(var maxLength: Int) : CappedCapacityMappingRule {
    override fun check(item: CappedCapacityMapping): MutableList<String> {
        val size = item.currency.length
        if (size > maxLength) {
            return mutableListOf<String>("currency is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedCapacityMappingRequiredCurrencyRule(var message: String) : CappedCapacityMappingRule {
    override fun check(item: CappedCapacityMapping): MutableList<String> {
        if (item.currency.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedCapacityMappingLabelLengthRule(var maxLength: Int) : CappedCapacityMappingRule {
    override fun check(item: CappedCapacityMapping): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedCapacityMappingCodeLengthRule(var maxLength: Int) : CappedCapacityMappingRule {
    override fun check(item: CappedCapacityMapping): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped capacity mapping. */
data class CappedCapacityMappingValidator(var rules: MutableList<CappedCapacityMappingRule>) {
    fun validate(item: CappedCapacityMapping): MutableList<String> {
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

    fun requireValid(item: CappedCapacityMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedCapacityMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedCapacityMappingValidator(): CappedCapacityMappingValidator {
    return CappedCapacityMappingValidator(
        mutableListOf<CappedCapacityMappingRule>(
            CappedCapacityMappingRequiredReviewerRule("capped capacity mapping needs an entry"),
            CappedCapacityMappingCurrencyLengthRule(3600),
            CappedCapacityMappingRequiredCurrencyRule("capped capacity mapping needs an entry"),
            CappedCapacityMappingLabelLengthRule(16),
            CappedCapacityMappingCodeLengthRule(255),
        ),
    )
}
