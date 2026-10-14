package com.acme.transport.customs.declaration.declarationmanifest

/** A rule a fallback declaration adjustment must pass. Check returns its problems, or none. */
interface FallbackDeclarationAdjustmentRule {
    fun check(item: FallbackDeclarationAdjustment): MutableList<String>
}

data class FallbackDeclarationAdjustmentRequiredNameRule(
    var message: String,
) : FallbackDeclarationAdjustmentRule {
    override fun check(item: FallbackDeclarationAdjustment): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackDeclarationAdjustmentNameLengthRule(var maxLength: Int) : FallbackDeclarationAdjustmentRule {
    override fun check(item: FallbackDeclarationAdjustment): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackDeclarationAdjustmentSummaryLengthRule(
    var maxLength: Int,
) : FallbackDeclarationAdjustmentRule {
    override fun check(item: FallbackDeclarationAdjustment): MutableList<String> {
        val size = item.summary.length
        if (size > maxLength) {
            return mutableListOf<String>("summary is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackDeclarationAdjustmentCarrierLengthRule(
    var maxLength: Int,
) : FallbackDeclarationAdjustmentRule {
    override fun check(item: FallbackDeclarationAdjustment): MutableList<String> {
        val size = item.carrier.length
        if (size > maxLength) {
            return mutableListOf<String>("carrier is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackDeclarationAdjustmentRequiredSummaryRule(
    var message: String,
) : FallbackDeclarationAdjustmentRule {
    override fun check(item: FallbackDeclarationAdjustment): MutableList<String> {
        if (item.summary.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackDeclarationAdjustmentRequiredCarrierRule(
    var message: String,
) : FallbackDeclarationAdjustmentRule {
    override fun check(item: FallbackDeclarationAdjustment): MutableList<String> {
        if (item.carrier.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback declaration adjustment. */
data class FallbackDeclarationAdjustmentValidator(var rules: MutableList<FallbackDeclarationAdjustmentRule>) {
    fun validate(item: FallbackDeclarationAdjustment): MutableList<String> {
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

    fun requireValid(item: FallbackDeclarationAdjustment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackDeclarationAdjustment>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackDeclarationAdjustmentValidator(): FallbackDeclarationAdjustmentValidator {
    return FallbackDeclarationAdjustmentValidator(
        mutableListOf<FallbackDeclarationAdjustmentRule>(
            FallbackDeclarationAdjustmentRequiredNameRule("fallback declaration adjustment needs a value"),
            FallbackDeclarationAdjustmentNameLengthRule(32),
            FallbackDeclarationAdjustmentSummaryLengthRule(255),
            FallbackDeclarationAdjustmentCarrierLengthRule(86400),
            FallbackDeclarationAdjustmentRequiredSummaryRule("fallback declaration adjustment needs a value"),
            FallbackDeclarationAdjustmentRequiredCarrierRule("fallback declaration adjustment needs this field"),
        ),
    )
}
