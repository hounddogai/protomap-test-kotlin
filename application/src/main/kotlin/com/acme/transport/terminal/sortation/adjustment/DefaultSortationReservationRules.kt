package com.acme.transport.terminal.sortation.adjustment

/** A rule a default sortation reservation must pass. Check returns its problems, or none. */
interface DefaultSortationReservationRule {
    fun check(item: DefaultSortationReservation): MutableList<String>
}

data class DefaultSortationReservationRequiredCodeRule(var message: String) : DefaultSortationReservationRule {
    override fun check(item: DefaultSortationReservation): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultSortationReservationCodePrefixRule(var prefix: String) : DefaultSortationReservationRule {
    override fun check(item: DefaultSortationReservation): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DefaultSortationReservationCodeLengthRule(var maxLength: Int) : DefaultSortationReservationRule {
    override fun check(item: DefaultSortationReservation): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default sortation reservation. */
data class DefaultSortationReservationValidator(var rules: MutableList<DefaultSortationReservationRule>) {
    fun validate(item: DefaultSortationReservation): MutableList<String> {
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

    fun requireValid(item: DefaultSortationReservation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultSortationReservation>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultSortationReservationValidator(): DefaultSortationReservationValidator {
    return DefaultSortationReservationValidator(
        mutableListOf<DefaultSortationReservationRule>(
            DefaultSortationReservationRequiredCodeRule("default sortation reservation needs a value"),
            DefaultSortationReservationCodePrefixRule("ACM"),
            DefaultSortationReservationCodeLengthRule(3600),
        ),
    )
}
