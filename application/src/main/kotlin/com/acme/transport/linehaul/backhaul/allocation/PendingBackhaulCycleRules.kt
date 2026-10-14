package com.acme.transport.linehaul.backhaul.allocation

/** A rule a pending backhaul cycle must pass. Check returns its problems, or none. */
interface PendingBackhaulCycleRule {
    fun check(item: PendingBackhaulCycle): MutableList<String>
}

data class PendingBackhaulCycleLocaleLengthRule(var maxLength: Int) : PendingBackhaulCycleRule {
    override fun check(item: PendingBackhaulCycle): MutableList<String> {
        val size = item.locale.length
        if (size > maxLength) {
            return mutableListOf<String>("locale is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingBackhaulCycleNameLengthRule(var maxLength: Int) : PendingBackhaulCycleRule {
    override fun check(item: PendingBackhaulCycle): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingBackhaulCycleRequiredNameRule(var message: String) : PendingBackhaulCycleRule {
    override fun check(item: PendingBackhaulCycle): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingBackhaulCycleRequiredVehicleIdRule(var message: String) : PendingBackhaulCycleRule {
    override fun check(item: PendingBackhaulCycle): MutableList<String> {
        if (item.vehicleId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending backhaul cycle. */
data class PendingBackhaulCycleValidator(var rules: MutableList<PendingBackhaulCycleRule>) {
    fun validate(item: PendingBackhaulCycle): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PendingBackhaulCycle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingBackhaulCycle>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingBackhaulCycleValidator(): PendingBackhaulCycleValidator {
    return PendingBackhaulCycleValidator(
        mutableListOf<PendingBackhaulCycleRule>(
            PendingBackhaulCycleLocaleLengthRule(1000),
            PendingBackhaulCycleNameLengthRule(16),
            PendingBackhaulCycleRequiredNameRule("pending backhaul cycle needs a value"),
            PendingBackhaulCycleRequiredVehicleIdRule("pending backhaul cycle needs an entry"),
        ),
    )
}
