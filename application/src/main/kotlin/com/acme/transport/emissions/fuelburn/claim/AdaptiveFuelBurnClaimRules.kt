package com.acme.transport.emissions.fuelburn.claim

/** A rule a adaptive fuel burn claim must pass. Check returns its problems, or none. */
interface AdaptiveFuelBurnClaimRule {
    fun check(item: AdaptiveFuelBurnClaim): MutableList<String>
}

data class AdaptiveFuelBurnClaimCodePrefixRule(var prefix: String) : AdaptiveFuelBurnClaimRule {
    override fun check(item: AdaptiveFuelBurnClaim): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveFuelBurnClaimCodeLengthRule(var maxLength: Int) : AdaptiveFuelBurnClaimRule {
    override fun check(item: AdaptiveFuelBurnClaim): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveFuelBurnClaimNameLengthRule(var maxLength: Int) : AdaptiveFuelBurnClaimRule {
    override fun check(item: AdaptiveFuelBurnClaim): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveFuelBurnClaimBatchSizeRangeRule(var min: Int, var max: Int) : AdaptiveFuelBurnClaimRule {
    override fun check(item: AdaptiveFuelBurnClaim): MutableList<String> {
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

data class AdaptiveFuelBurnClaimCreatedAtRangeRule(var min: Int, var max: Int) : AdaptiveFuelBurnClaimRule {
    override fun check(item: AdaptiveFuelBurnClaim): MutableList<String> {
        val value = item.createdAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("created at $value is below $min")
        }
        if (value > max) {
            problems.add("created at $value is above $max")
        }
        return problems
    }
}

data class AdaptiveFuelBurnClaimDescriptionLengthRule(var maxLength: Int) : AdaptiveFuelBurnClaimRule {
    override fun check(item: AdaptiveFuelBurnClaim): MutableList<String> {
        val size = item.description.length
        if (size > maxLength) {
            return mutableListOf<String>("description is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive fuel burn claim. */
data class AdaptiveFuelBurnClaimValidator(var rules: MutableList<AdaptiveFuelBurnClaimRule>) {
    fun validate(item: AdaptiveFuelBurnClaim): MutableList<String> {
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

    fun requireValid(item: AdaptiveFuelBurnClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveFuelBurnClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveFuelBurnClaimValidator(): AdaptiveFuelBurnClaimValidator {
    return AdaptiveFuelBurnClaimValidator(
        mutableListOf<AdaptiveFuelBurnClaimRule>(
            AdaptiveFuelBurnClaimCodePrefixRule("REF"),
            AdaptiveFuelBurnClaimCodeLengthRule(255),
            AdaptiveFuelBurnClaimNameLengthRule(1000),
            AdaptiveFuelBurnClaimBatchSizeRangeRule(80, 3600),
            AdaptiveFuelBurnClaimCreatedAtRangeRule(255, 86400),
            AdaptiveFuelBurnClaimDescriptionLengthRule(3600),
        ),
    )
}
