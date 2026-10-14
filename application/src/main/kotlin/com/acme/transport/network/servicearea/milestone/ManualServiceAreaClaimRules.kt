package com.acme.transport.network.servicearea.milestone

/** A rule a manual service area claim must pass. Check returns its problems, or none. */
interface ManualServiceAreaClaimRule {
    fun check(item: ManualServiceAreaClaim): MutableList<String>
}

data class ManualServiceAreaClaimRequiredTrailerIdRule(var message: String) : ManualServiceAreaClaimRule {
    override fun check(item: ManualServiceAreaClaim): MutableList<String> {
        if (item.trailerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualServiceAreaClaimBatchSizeRangeRule(var min: Int, var max: Int) : ManualServiceAreaClaimRule {
    override fun check(item: ManualServiceAreaClaim): MutableList<String> {
        val value = item.batchSize
        if (value < min || value > max) {
            return mutableListOf<String>("batch size $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ManualServiceAreaClaimRequiredCodeRule(var message: String) : ManualServiceAreaClaimRule {
    override fun check(item: ManualServiceAreaClaim): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualServiceAreaClaimTrailerIdLengthRule(var maxLength: Int) : ManualServiceAreaClaimRule {
    override fun check(item: ManualServiceAreaClaim): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualServiceAreaClaimCodePrefixRule(var prefix: String) : ManualServiceAreaClaimRule {
    override fun check(item: ManualServiceAreaClaim): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ManualServiceAreaClaimLabelLengthRule(var maxLength: Int) : ManualServiceAreaClaimRule {
    override fun check(item: ManualServiceAreaClaim): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual service area claim. */
data class ManualServiceAreaClaimValidator(var rules: MutableList<ManualServiceAreaClaimRule>) {
    fun validate(item: ManualServiceAreaClaim): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ManualServiceAreaClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualServiceAreaClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultManualServiceAreaClaimValidator(): ManualServiceAreaClaimValidator {
    return ManualServiceAreaClaimValidator(
        mutableListOf<ManualServiceAreaClaimRule>(
            ManualServiceAreaClaimRequiredTrailerIdRule("manual service area claim needs this field"),
            ManualServiceAreaClaimBatchSizeRangeRule(16, 80),
            ManualServiceAreaClaimRequiredCodeRule("manual service area claim needs a value"),
            ManualServiceAreaClaimTrailerIdLengthRule(16),
            ManualServiceAreaClaimCodePrefixRule("OPS"),
            ManualServiceAreaClaimLabelLengthRule(3600),
        ),
    )
}
