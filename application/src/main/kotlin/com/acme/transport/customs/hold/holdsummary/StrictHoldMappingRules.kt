package com.acme.transport.customs.hold.holdsummary

/** A rule a strict hold mapping must pass. Check returns its problems, or none. */
interface StrictHoldMappingRule {
    fun check(item: StrictHoldMapping): MutableList<String>
}

data class StrictHoldMappingRequiredTitleRule(var message: String) : StrictHoldMappingRule {
    override fun check(item: StrictHoldMapping): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictHoldMappingCapacityRangeRule(var min: Int, var max: Int) : StrictHoldMappingRule {
    override fun check(item: StrictHoldMapping): MutableList<String> {
        val value = item.capacity
        if (value < min || value > max) {
            return mutableListOf<String>("capacity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class StrictHoldMappingRequiredLaneRule(var message: String) : StrictHoldMappingRule {
    override fun check(item: StrictHoldMapping): MutableList<String> {
        if (item.lane.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict hold mapping. */
data class StrictHoldMappingValidator(var rules: MutableList<StrictHoldMappingRule>) {
    fun validate(item: StrictHoldMapping): MutableList<String> {
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

    fun requireValid(item: StrictHoldMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictHoldMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictHoldMappingValidator(): StrictHoldMappingValidator {
    return StrictHoldMappingValidator(
        mutableListOf<StrictHoldMappingRule>(
            StrictHoldMappingRequiredTitleRule("strict hold mapping needs an entry"),
            StrictHoldMappingCapacityRangeRule(120, 3600),
            StrictHoldMappingRequiredLaneRule("strict hold mapping needs this field"),
        ),
    )
}
