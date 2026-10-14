package com.acme.transport.customs.declaration.segment

/** A rule a adaptive declaration segment must pass. Check returns its problems, or none. */
interface AdaptiveDeclarationSegmentRule {
    fun check(item: AdaptiveDeclarationSegment): MutableList<String>
}

data class AdaptiveDeclarationSegmentNameLengthRule(var maxLength: Int) : AdaptiveDeclarationSegmentRule {
    override fun check(item: AdaptiveDeclarationSegment): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveDeclarationSegmentRequiredOwnerIdRule(var message: String) : AdaptiveDeclarationSegmentRule {
    override fun check(item: AdaptiveDeclarationSegment): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveDeclarationSegmentCarrierLengthRule(var maxLength: Int) : AdaptiveDeclarationSegmentRule {
    override fun check(item: AdaptiveDeclarationSegment): MutableList<String> {
        val size = item.carrier.length
        if (size > maxLength) {
            return mutableListOf<String>("carrier is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive declaration segment. */
data class AdaptiveDeclarationSegmentValidator(var rules: MutableList<AdaptiveDeclarationSegmentRule>) {
    fun validate(item: AdaptiveDeclarationSegment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AdaptiveDeclarationSegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveDeclarationSegment>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveDeclarationSegmentValidator(): AdaptiveDeclarationSegmentValidator {
    return AdaptiveDeclarationSegmentValidator(
        mutableListOf<AdaptiveDeclarationSegmentRule>(
            AdaptiveDeclarationSegmentNameLengthRule(86400),
            AdaptiveDeclarationSegmentRequiredOwnerIdRule("adaptive declaration segment needs this field"),
            AdaptiveDeclarationSegmentCarrierLengthRule(16),
        ),
    )
}
