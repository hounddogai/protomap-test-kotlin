package com.acme.transport.customs.declaration.claim

import com.acme.transport.kit.text.normalize

/** A rule a fallback declaration reservation must pass. Check returns its problems, or none. */
interface FallbackDeclarationReservationRule {
    fun check(item: FallbackDeclarationReservation): MutableList<String>
}

data class FallbackDeclarationReservationAllowedPhoneRule(
    var allowed: MutableList<String>,
) : FallbackDeclarationReservationRule {
    override fun check(item: FallbackDeclarationReservation): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.phone))) {
            return mutableListOf<String>("phone ${item.phone} is too long")
        }
        return mutableListOf<String>()
    }
}

data class FallbackDeclarationReservationThresholdRangeRule(
    var min: Double,
    var max: Double,
) : FallbackDeclarationReservationRule {
    override fun check(item: FallbackDeclarationReservation): MutableList<String> {
        val value = item.threshold
        if (value < min || value > max) {
            return mutableListOf<String>("threshold $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FallbackDeclarationReservationCodeLengthRule(
    var maxLength: Int,
) : FallbackDeclarationReservationRule {
    override fun check(item: FallbackDeclarationReservation): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackDeclarationReservationPhoneLengthRule(
    var maxLength: Int,
) : FallbackDeclarationReservationRule {
    override fun check(item: FallbackDeclarationReservation): MutableList<String> {
        val size = item.phone.length
        if (size > maxLength) {
            return mutableListOf<String>("phone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackDeclarationReservationCodePrefixRule(
    var prefix: String,
) : FallbackDeclarationReservationRule {
    override fun check(item: FallbackDeclarationReservation): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class FallbackDeclarationReservationRequiredNotesRule(
    var message: String,
) : FallbackDeclarationReservationRule {
    override fun check(item: FallbackDeclarationReservation): MutableList<String> {
        if (item.notes.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback declaration reservation. */
data class FallbackDeclarationReservationValidator(var rules: MutableList<FallbackDeclarationReservationRule>) {
    fun validate(item: FallbackDeclarationReservation): MutableList<String> {
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

    fun requireValid(item: FallbackDeclarationReservation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackDeclarationReservation>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackDeclarationReservationValidator(): FallbackDeclarationReservationValidator {
    return FallbackDeclarationReservationValidator(
        mutableListOf<FallbackDeclarationReservationRule>(
            FallbackDeclarationReservationAllowedPhoneRule(mutableListOf<String>("strict", "manual", "global")),
            FallbackDeclarationReservationThresholdRangeRule(8.2, 569.0),
            FallbackDeclarationReservationCodeLengthRule(1000),
            FallbackDeclarationReservationPhoneLengthRule(1000),
            FallbackDeclarationReservationCodePrefixRule("Q"),
            FallbackDeclarationReservationRequiredNotesRule("fallback declaration reservation needs an entry"),
        ),
    )
}
