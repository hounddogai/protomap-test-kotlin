package com.acme.transport.crewing.availability.availabilitysegment

/** A rule a default availability agreement must pass. Check returns its problems, or none. */
interface DefaultAvailabilityAgreementRule {
    fun check(item: DefaultAvailabilityAgreement): MutableList<String>
}

data class DefaultAvailabilityAgreementOwnerIdLengthRule(
    var maxLength: Int,
) : DefaultAvailabilityAgreementRule {
    override fun check(item: DefaultAvailabilityAgreement): MutableList<String> {
        val size = item.ownerId.length
        if (size > maxLength) {
            return mutableListOf<String>("owner id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultAvailabilityAgreementPinnedGuardRule(var limit: Double) : DefaultAvailabilityAgreementRule {
    override fun check(item: DefaultAvailabilityAgreement): MutableList<String> {
        if (item.pinned && item.score > limit) {
            return mutableListOf<String>("score ${item.score} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

data class DefaultAvailabilityAgreementLabelLengthRule(var maxLength: Int) : DefaultAvailabilityAgreementRule {
    override fun check(item: DefaultAvailabilityAgreement): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultAvailabilityAgreementScoreRangeRule(
    var min: Double,
    var max: Double,
) : DefaultAvailabilityAgreementRule {
    override fun check(item: DefaultAvailabilityAgreement): MutableList<String> {
        val value = item.score
        if (value < min || value > max) {
            return mutableListOf<String>("score $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DefaultAvailabilityAgreementCodePrefixRule(var prefix: String) : DefaultAvailabilityAgreementRule {
    override fun check(item: DefaultAvailabilityAgreement): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DefaultAvailabilityAgreementRequiredLabelRule(
    var message: String,
) : DefaultAvailabilityAgreementRule {
    override fun check(item: DefaultAvailabilityAgreement): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default availability agreement. */
data class DefaultAvailabilityAgreementValidator(var rules: MutableList<DefaultAvailabilityAgreementRule>) {
    fun validate(item: DefaultAvailabilityAgreement): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefaultAvailabilityAgreement) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultAvailabilityAgreement>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultAvailabilityAgreementValidator(): DefaultAvailabilityAgreementValidator {
    return DefaultAvailabilityAgreementValidator(
        mutableListOf<DefaultAvailabilityAgreementRule>(
            DefaultAvailabilityAgreementOwnerIdLengthRule(255),
            DefaultAvailabilityAgreementPinnedGuardRule(0.3),
            DefaultAvailabilityAgreementLabelLengthRule(64),
            DefaultAvailabilityAgreementScoreRangeRule(0.3, 4559.6),
            DefaultAvailabilityAgreementCodePrefixRule("X"),
            DefaultAvailabilityAgreementRequiredLabelRule("default availability agreement needs a value"),
        ),
    )
}
