package com.acme.transport.customs.declaration.schedule

/** A rule a adaptive declaration reservation must pass. Check returns its problems, or none. */
interface AdaptiveDeclarationReservationRule {
    fun check(item: AdaptiveDeclarationReservation): MutableList<String>
}

data class AdaptiveDeclarationReservationCodeLengthRule(
    var maxLength: Int,
) : AdaptiveDeclarationReservationRule {
    override fun check(item: AdaptiveDeclarationReservation): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveDeclarationReservationNameLengthRule(
    var maxLength: Int,
) : AdaptiveDeclarationReservationRule {
    override fun check(item: AdaptiveDeclarationReservation): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveDeclarationReservationCapacityRangeRule(
    var min: Int,
    var max: Int,
) : AdaptiveDeclarationReservationRule {
    override fun check(item: AdaptiveDeclarationReservation): MutableList<String> {
        val value = item.capacity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("capacity $value is below $min")
        }
        if (value > max) {
            problems.add("capacity $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a adaptive declaration reservation. */
data class AdaptiveDeclarationReservationValidator(var rules: MutableList<AdaptiveDeclarationReservationRule>) {
    fun validate(item: AdaptiveDeclarationReservation): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AdaptiveDeclarationReservation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveDeclarationReservation>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveDeclarationReservationValidator(): AdaptiveDeclarationReservationValidator {
    return AdaptiveDeclarationReservationValidator(
        mutableListOf<AdaptiveDeclarationReservationRule>(
            AdaptiveDeclarationReservationCodeLengthRule(120),
            AdaptiveDeclarationReservationNameLengthRule(255),
            AdaptiveDeclarationReservationCapacityRangeRule(16, 16),
        ),
    )
}
