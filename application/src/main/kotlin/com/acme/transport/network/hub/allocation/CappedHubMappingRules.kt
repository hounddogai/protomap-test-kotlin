package com.acme.transport.network.hub.allocation

/** A rule a capped hub mapping must pass. Check returns its problems, or none. */
interface CappedHubMappingRule {
    fun check(item: CappedHubMapping): MutableList<String>
}

data class CappedHubMappingCodeLengthRule(var maxLength: Int) : CappedHubMappingRule {
    override fun check(item: CappedHubMapping): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedHubMappingRequiredOwnerIdRule(var message: String) : CappedHubMappingRule {
    override fun check(item: CappedHubMapping): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedHubMappingRetriesRangeRule(var min: Int, var max: Int) : CappedHubMappingRule {
    override fun check(item: CappedHubMapping): MutableList<String> {
        val value = item.retries
        if (value < min || value > max) {
            return mutableListOf<String>("retries $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CappedHubMappingRequiredCodeRule(var message: String) : CappedHubMappingRule {
    override fun check(item: CappedHubMapping): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedHubMappingBatchSizeRangeRule(var min: Int, var max: Int) : CappedHubMappingRule {
    override fun check(item: CappedHubMapping): MutableList<String> {
        val value = item.batchSize
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("batch size $value is below $min")
        }
        if (value > max) {
            problems.add("batch size $value is above $max")
        }
        return problems
    }
}

data class CappedHubMappingCountryLengthRule(var maxLength: Int) : CappedHubMappingRule {
    override fun check(item: CappedHubMapping): MutableList<String> {
        val size = item.country.length
        if (size > maxLength) {
            return mutableListOf<String>("country is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped hub mapping. */
data class CappedHubMappingValidator(var rules: MutableList<CappedHubMappingRule>) {
    fun validate(item: CappedHubMapping): MutableList<String> {
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

    fun requireValid(item: CappedHubMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedHubMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedHubMappingValidator(): CappedHubMappingValidator {
    return CappedHubMappingValidator(
        mutableListOf<CappedHubMappingRule>(
            CappedHubMappingCodeLengthRule(32),
            CappedHubMappingRequiredOwnerIdRule("capped hub mapping needs this field"),
            CappedHubMappingRetriesRangeRule(80, 3600),
            CappedHubMappingRequiredCodeRule("capped hub mapping needs this field"),
            CappedHubMappingBatchSizeRangeRule(120, 255),
            CappedHubMappingCountryLengthRule(86400),
        ),
    )
}
