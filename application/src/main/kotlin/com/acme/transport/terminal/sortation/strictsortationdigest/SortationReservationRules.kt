package com.acme.transport.terminal.sortation.strictsortationdigest

/** A rule a sortation reservation must pass. Check returns its problems, or none. */
interface SortationReservationRule {
    fun check(item: SortationReservation): MutableList<String>
}

data class SortationReservationTitleLengthRule(var maxLength: Int) : SortationReservationRule {
    override fun check(item: SortationReservation): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SortationReservationCodeLengthRule(var maxLength: Int) : SortationReservationRule {
    override fun check(item: SortationReservation): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SortationReservationQuantityRangeRule(var min: Int, var max: Int) : SortationReservationRule {
    override fun check(item: SortationReservation): MutableList<String> {
        val value = item.quantity
        if (value < min || value > max) {
            return mutableListOf<String>("quantity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a sortation reservation. */
data class SortationReservationValidator(var rules: MutableList<SortationReservationRule>) {
    fun validate(item: SortationReservation): MutableList<String> {
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

    fun requireValid(item: SortationReservation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SortationReservation>): MutableMap<String, MutableList<String>> {
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

fun defaultSortationReservationValidator(): SortationReservationValidator {
    return SortationReservationValidator(
        mutableListOf<SortationReservationRule>(
            SortationReservationTitleLengthRule(80),
            SortationReservationCodeLengthRule(255),
            SortationReservationQuantityRangeRule(16, 80),
        ),
    )
}
