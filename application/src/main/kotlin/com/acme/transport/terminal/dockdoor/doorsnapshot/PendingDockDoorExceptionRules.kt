package com.acme.transport.terminal.dockdoor.doorsnapshot

/** A rule a pending dock door exception must pass. Check returns its problems, or none. */
interface PendingDockDoorExceptionRule {
    fun check(item: PendingDockDoorException): MutableList<String>
}

data class PendingDockDoorExceptionTitleLengthRule(var maxLength: Int) : PendingDockDoorExceptionRule {
    override fun check(item: PendingDockDoorException): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingDockDoorExceptionRequiredVehicleIdRule(var message: String) : PendingDockDoorExceptionRule {
    override fun check(item: PendingDockDoorException): MutableList<String> {
        if (item.vehicleId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingDockDoorExceptionRequiredPhoneRule(var message: String) : PendingDockDoorExceptionRule {
    override fun check(item: PendingDockDoorException): MutableList<String> {
        if (item.phone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingDockDoorExceptionRatioRangeRule(
    var min: Double,
    var max: Double,
) : PendingDockDoorExceptionRule {
    override fun check(item: PendingDockDoorException): MutableList<String> {
        val value = item.ratio
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ratio $value is below $min")
        }
        if (value > max) {
            problems.add("ratio $value is above $max")
        }
        return problems
    }
}

data class PendingDockDoorExceptionMarginRangeRule(
    var min: Double,
    var max: Double,
) : PendingDockDoorExceptionRule {
    override fun check(item: PendingDockDoorException): MutableList<String> {
        val value = item.margin
        if (value < min || value > max) {
            return mutableListOf<String>("margin $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PendingDockDoorExceptionRequiredCodeRule(var message: String) : PendingDockDoorExceptionRule {
    override fun check(item: PendingDockDoorException): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending dock door exception. */
data class PendingDockDoorExceptionValidator(var rules: MutableList<PendingDockDoorExceptionRule>) {
    fun validate(item: PendingDockDoorException): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PendingDockDoorException) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingDockDoorException>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingDockDoorExceptionValidator(): PendingDockDoorExceptionValidator {
    return PendingDockDoorExceptionValidator(
        mutableListOf<PendingDockDoorExceptionRule>(
            PendingDockDoorExceptionTitleLengthRule(255),
            PendingDockDoorExceptionRequiredVehicleIdRule("pending dock door exception needs this field"),
            PendingDockDoorExceptionRequiredPhoneRule("pending dock door exception needs an entry"),
            PendingDockDoorExceptionRatioRangeRule(2.8, 954.6),
            PendingDockDoorExceptionMarginRangeRule(3.2, 4966.4),
            PendingDockDoorExceptionRequiredCodeRule("pending dock door exception needs a value"),
        ),
    )
}
