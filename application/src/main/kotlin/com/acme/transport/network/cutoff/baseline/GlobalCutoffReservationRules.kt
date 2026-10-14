package com.acme.transport.network.cutoff.baseline

/** A rule a global cutoff reservation must pass. Check returns its problems, or none. */
interface GlobalCutoffReservationRule {
    fun check(item: GlobalCutoffReservation): MutableList<String>
}

data class GlobalCutoffReservationGraceDaysRangeRule(var min: Int, var max: Int) : GlobalCutoffReservationRule {
    override fun check(item: GlobalCutoffReservation): MutableList<String> {
        val value = item.graceDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("grace days $value is below $min")
        }
        if (value > max) {
            problems.add("grace days $value is above $max")
        }
        return problems
    }
}

data class GlobalCutoffReservationRequiredCodeRule(var message: String) : GlobalCutoffReservationRule {
    override fun check(item: GlobalCutoffReservation): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalCutoffReservationAttemptsRangeRule(var min: Int, var max: Int) : GlobalCutoffReservationRule {
    override fun check(item: GlobalCutoffReservation): MutableList<String> {
        val value = item.attempts
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("attempts $value is below $min")
        }
        if (value > max) {
            problems.add("attempts $value is above $max")
        }
        return problems
    }
}

data class GlobalCutoffReservationRetriesRangeRule(var min: Int, var max: Int) : GlobalCutoffReservationRule {
    override fun check(item: GlobalCutoffReservation): MutableList<String> {
        val value = item.retries
        if (value < min || value > max) {
            return mutableListOf<String>("retries $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class GlobalCutoffReservationNameLengthRule(var maxLength: Int) : GlobalCutoffReservationRule {
    override fun check(item: GlobalCutoffReservation): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalCutoffReservationCodePrefixRule(var prefix: String) : GlobalCutoffReservationRule {
    override fun check(item: GlobalCutoffReservation): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global cutoff reservation. */
data class GlobalCutoffReservationValidator(var rules: MutableList<GlobalCutoffReservationRule>) {
    fun validate(item: GlobalCutoffReservation): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: GlobalCutoffReservation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalCutoffReservation>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalCutoffReservationValidator(): GlobalCutoffReservationValidator {
    return GlobalCutoffReservationValidator(
        mutableListOf<GlobalCutoffReservationRule>(
            GlobalCutoffReservationGraceDaysRangeRule(16, 32),
            GlobalCutoffReservationRequiredCodeRule("global cutoff reservation needs a value"),
            GlobalCutoffReservationAttemptsRangeRule(120, 86400),
            GlobalCutoffReservationRetriesRangeRule(16, 80),
            GlobalCutoffReservationNameLengthRule(80),
            GlobalCutoffReservationCodePrefixRule("ACM"),
        ),
    )
}
