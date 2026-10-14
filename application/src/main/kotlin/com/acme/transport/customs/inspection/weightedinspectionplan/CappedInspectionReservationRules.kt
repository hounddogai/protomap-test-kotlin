package com.acme.transport.customs.inspection.weightedinspectionplan

/** A rule a capped inspection reservation must pass. Check returns its problems, or none. */
interface CappedInspectionReservationRule {
    fun check(item: CappedInspectionReservation): MutableList<String>
}

data class CappedInspectionReservationRequiredCodeRule(var message: String) : CappedInspectionReservationRule {
    override fun check(item: CappedInspectionReservation): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedInspectionReservationBatchSizeRangeRule(
    var min: Int,
    var max: Int,
) : CappedInspectionReservationRule {
    override fun check(item: CappedInspectionReservation): MutableList<String> {
        val value = item.batchSize
        if (value < min || value > max) {
            return mutableListOf<String>("batch size $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CappedInspectionReservationRequiredTitleRule(var message: String) : CappedInspectionReservationRule {
    override fun check(item: CappedInspectionReservation): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped inspection reservation. */
data class CappedInspectionReservationValidator(var rules: MutableList<CappedInspectionReservationRule>) {
    fun validate(item: CappedInspectionReservation): MutableList<String> {
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

    fun requireValid(item: CappedInspectionReservation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedInspectionReservation>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedInspectionReservationValidator(): CappedInspectionReservationValidator {
    return CappedInspectionReservationValidator(
        mutableListOf<CappedInspectionReservationRule>(
            CappedInspectionReservationRequiredCodeRule("capped inspection reservation needs this field"),
            CappedInspectionReservationBatchSizeRangeRule(64, 80),
            CappedInspectionReservationRequiredTitleRule("capped inspection reservation needs a value"),
        ),
    )
}
