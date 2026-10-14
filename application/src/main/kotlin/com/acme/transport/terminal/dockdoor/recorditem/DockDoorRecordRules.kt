package com.acme.transport.terminal.dockdoor.recorditem

/** A rule a dock door record must pass. Check returns its problems, or none. */
interface DockDoorRecordRule {
    fun check(item: DockDoorRecord): MutableList<String>
}

data class DockDoorRecordDistanceKmRangeRule(var min: Double, var max: Double) : DockDoorRecordRule {
    override fun check(item: DockDoorRecord): MutableList<String> {
        val value = item.distanceKm
        if (value < min || value > max) {
            return mutableListOf<String>("distance km $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DockDoorRecordTitleLengthRule(var maxLength: Int) : DockDoorRecordRule {
    override fun check(item: DockDoorRecord): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DockDoorRecordRouteCodeLengthRule(var maxLength: Int) : DockDoorRecordRule {
    override fun check(item: DockDoorRecord): MutableList<String> {
        val size = item.routeCode.length
        if (size > maxLength) {
            return mutableListOf<String>("route code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DockDoorRecordRequiredTitleRule(var message: String) : DockDoorRecordRule {
    override fun check(item: DockDoorRecord): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DockDoorRecordRequiredRouteCodeRule(var message: String) : DockDoorRecordRule {
    override fun check(item: DockDoorRecord): MutableList<String> {
        if (item.routeCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a dock door record. */
data class DockDoorRecordValidator(var rules: MutableList<DockDoorRecordRule>) {
    fun validate(item: DockDoorRecord): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DockDoorRecord) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DockDoorRecord>): MutableMap<String, MutableList<String>> {
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

fun defaultDockDoorRecordValidator(): DockDoorRecordValidator {
    return DockDoorRecordValidator(
        mutableListOf<DockDoorRecordRule>(
            DockDoorRecordDistanceKmRangeRule(8.4, 1206.6),
            DockDoorRecordTitleLengthRule(80),
            DockDoorRecordRouteCodeLengthRule(80),
            DockDoorRecordRequiredTitleRule("dock door record needs a value"),
            DockDoorRecordRequiredRouteCodeRule("dock door record needs this field"),
        ),
    )
}
