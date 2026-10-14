package com.acme.transport.customs.declaration.declarationforecast

/** A rule a pending declaration reservation must pass. Check returns its problems, or none. */
interface PendingDeclarationReservationRule {
    fun check(item: PendingDeclarationReservation): MutableList<String>
}

data class PendingDeclarationReservationRequiredLabelRule(
    var message: String,
) : PendingDeclarationReservationRule {
    override fun check(item: PendingDeclarationReservation): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingDeclarationReservationRequiredCodeRule(
    var message: String,
) : PendingDeclarationReservationRule {
    override fun check(item: PendingDeclarationReservation): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingDeclarationReservationRateRangeRule(
    var min: Double,
    var max: Double,
) : PendingDeclarationReservationRule {
    override fun check(item: PendingDeclarationReservation): MutableList<String> {
        val value = item.rate
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("rate $value is below $min")
        }
        if (value > max) {
            problems.add("rate $value is above $max")
        }
        return problems
    }
}

data class PendingDeclarationReservationDriverIdLengthRule(
    var maxLength: Int,
) : PendingDeclarationReservationRule {
    override fun check(item: PendingDeclarationReservation): MutableList<String> {
        val size = item.driverId.length
        if (size > maxLength) {
            return mutableListOf<String>("driver id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingDeclarationReservationRequiredDriverIdRule(
    var message: String,
) : PendingDeclarationReservationRule {
    override fun check(item: PendingDeclarationReservation): MutableList<String> {
        if (item.driverId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending declaration reservation. */
data class PendingDeclarationReservationValidator(var rules: MutableList<PendingDeclarationReservationRule>) {
    fun validate(item: PendingDeclarationReservation): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PendingDeclarationReservation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingDeclarationReservation>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingDeclarationReservationValidator(): PendingDeclarationReservationValidator {
    return PendingDeclarationReservationValidator(
        mutableListOf<PendingDeclarationReservationRule>(
            PendingDeclarationReservationRequiredLabelRule("pending declaration reservation needs this field"),
            PendingDeclarationReservationRequiredCodeRule("pending declaration reservation needs this field"),
            PendingDeclarationReservationRateRangeRule(0.6, 3465.8),
            PendingDeclarationReservationDriverIdLengthRule(64),
            PendingDeclarationReservationRequiredDriverIdRule(
                "pending declaration reservation needs this field",
            ),
        ),
    )
}
