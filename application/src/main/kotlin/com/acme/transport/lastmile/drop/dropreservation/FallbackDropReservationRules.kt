package com.acme.transport.lastmile.drop.dropreservation

/** A rule a fallback drop reservation must pass. Check returns its problems, or none. */
interface FallbackDropReservationRule {
    fun check(item: FallbackDropReservation): MutableList<String>
}

data class FallbackDropReservationLabelLengthRule(var maxLength: Int) : FallbackDropReservationRule {
    override fun check(item: FallbackDropReservation): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackDropReservationCodePrefixRule(var prefix: String) : FallbackDropReservationRule {
    override fun check(item: FallbackDropReservation): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class FallbackDropReservationTimezoneLengthRule(var maxLength: Int) : FallbackDropReservationRule {
    override fun check(item: FallbackDropReservation): MutableList<String> {
        val size = item.timezone.length
        if (size > maxLength) {
            return mutableListOf<String>("timezone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackDropReservationRevisionRangeRule(var min: Int, var max: Int) : FallbackDropReservationRule {
    override fun check(item: FallbackDropReservation): MutableList<String> {
        val value = item.revision
        if (value < min || value > max) {
            return mutableListOf<String>("revision $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback drop reservation. */
data class FallbackDropReservationValidator(var rules: MutableList<FallbackDropReservationRule>) {
    fun validate(item: FallbackDropReservation): MutableList<String> {
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

    fun requireValid(item: FallbackDropReservation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackDropReservation>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackDropReservationValidator(): FallbackDropReservationValidator {
    return FallbackDropReservationValidator(
        mutableListOf<FallbackDropReservationRule>(
            FallbackDropReservationLabelLengthRule(32),
            FallbackDropReservationCodePrefixRule("REF"),
            FallbackDropReservationTimezoneLengthRule(120),
            FallbackDropReservationRevisionRangeRule(64, 80),
        ),
    )
}
