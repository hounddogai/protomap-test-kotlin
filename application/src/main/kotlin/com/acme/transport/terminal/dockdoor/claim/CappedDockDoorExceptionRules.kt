package com.acme.transport.terminal.dockdoor.claim

/** A rule a capped dock door exception must pass. Check returns its problems, or none. */
interface CappedDockDoorExceptionRule {
    fun check(item: CappedDockDoorException): MutableList<String>
}

data class CappedDockDoorExceptionCodeLengthRule(var maxLength: Int) : CappedDockDoorExceptionRule {
    override fun check(item: CappedDockDoorException): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedDockDoorExceptionDistanceKmRangeRule(
    var min: Double,
    var max: Double,
) : CappedDockDoorExceptionRule {
    override fun check(item: CappedDockDoorException): MutableList<String> {
        val value = item.distanceKm
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("distance km $value is below $min")
        }
        if (value > max) {
            problems.add("distance km $value is above $max")
        }
        return problems
    }
}

data class CappedDockDoorExceptionRequiredExternalRefRule(var message: String) : CappedDockDoorExceptionRule {
    override fun check(item: CappedDockDoorException): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedDockDoorExceptionRequiredDockRule(var message: String) : CappedDockDoorExceptionRule {
    override fun check(item: CappedDockDoorException): MutableList<String> {
        if (item.dock.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped dock door exception. */
data class CappedDockDoorExceptionValidator(var rules: MutableList<CappedDockDoorExceptionRule>) {
    fun validate(item: CappedDockDoorException): MutableList<String> {
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

    fun requireValid(item: CappedDockDoorException) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedDockDoorException>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedDockDoorExceptionValidator(): CappedDockDoorExceptionValidator {
    return CappedDockDoorExceptionValidator(
        mutableListOf<CappedDockDoorExceptionRule>(
            CappedDockDoorExceptionCodeLengthRule(1000),
            CappedDockDoorExceptionDistanceKmRangeRule(4.3, 3844.8),
            CappedDockDoorExceptionRequiredExternalRefRule("capped dock door exception needs this field"),
            CappedDockDoorExceptionRequiredDockRule("capped dock door exception needs an entry"),
        ),
    )
}
