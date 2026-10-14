package com.acme.transport.emissions.fuelburn.burnwindow

/** A rule a pending fuel burn window must pass. Check returns its problems, or none. */
interface PendingFuelBurnWindowRule {
    fun check(item: PendingFuelBurnWindow): MutableList<String>
}

data class PendingFuelBurnWindowRequiredShiftCodeRule(var message: String) : PendingFuelBurnWindowRule {
    override fun check(item: PendingFuelBurnWindow): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingFuelBurnWindowUnitLengthRule(var maxLength: Int) : PendingFuelBurnWindowRule {
    override fun check(item: PendingFuelBurnWindow): MutableList<String> {
        val size = item.unit.length
        if (size > maxLength) {
            return mutableListOf<String>("unit is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingFuelBurnWindowShiftCodeLengthRule(var maxLength: Int) : PendingFuelBurnWindowRule {
    override fun check(item: PendingFuelBurnWindow): MutableList<String> {
        val size = item.shiftCode.length
        if (size > maxLength) {
            return mutableListOf<String>("shift code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending fuel burn window. */
data class PendingFuelBurnWindowValidator(var rules: MutableList<PendingFuelBurnWindowRule>) {
    fun validate(item: PendingFuelBurnWindow): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PendingFuelBurnWindow) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingFuelBurnWindow>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingFuelBurnWindowValidator(): PendingFuelBurnWindowValidator {
    return PendingFuelBurnWindowValidator(
        mutableListOf<PendingFuelBurnWindowRule>(
            PendingFuelBurnWindowRequiredShiftCodeRule("pending fuel burn window needs a value"),
            PendingFuelBurnWindowUnitLengthRule(16),
            PendingFuelBurnWindowShiftCodeLengthRule(16),
        ),
    )
}
