package com.acme.transport.linehaul.capacity.metric

/** A rule a capacity window must pass. Check returns its problems, or none. */
interface CapacityWindowRule {
    fun check(item: CapacityWindow): MutableList<String>
}

data class CapacityWindowUnitLengthRule(var maxLength: Int) : CapacityWindowRule {
    override fun check(item: CapacityWindow): MutableList<String> {
        val size = item.unit.length
        if (size > maxLength) {
            return mutableListOf<String>("unit is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CapacityWindowVisibleGuardRule(var limit: Double) : CapacityWindowRule {
    override fun check(item: CapacityWindow): MutableList<String> {
        if (item.visible && item.cost > limit) {
            return mutableListOf<String>("cost ${item.cost} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

data class CapacityWindowCodeLengthRule(var maxLength: Int) : CapacityWindowRule {
    override fun check(item: CapacityWindow): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CapacityWindowDepotLengthRule(var maxLength: Int) : CapacityWindowRule {
    override fun check(item: CapacityWindow): MutableList<String> {
        val size = item.depot.length
        if (size > maxLength) {
            return mutableListOf<String>("depot is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capacity window. */
data class CapacityWindowValidator(var rules: MutableList<CapacityWindowRule>) {
    fun validate(item: CapacityWindow): MutableList<String> {
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

    fun requireValid(item: CapacityWindow) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CapacityWindow>): MutableMap<String, MutableList<String>> {
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

fun defaultCapacityWindowValidator(): CapacityWindowValidator {
    return CapacityWindowValidator(
        mutableListOf<CapacityWindowRule>(
            CapacityWindowUnitLengthRule(32),
            CapacityWindowVisibleGuardRule(6.7),
            CapacityWindowCodeLengthRule(32),
            CapacityWindowDepotLengthRule(80),
        ),
    )
}
