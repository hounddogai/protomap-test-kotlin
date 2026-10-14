package com.acme.transport.emissions.evcharge.claim

/** A rule a ev charge milestone must pass. Check returns its problems, or none. */
interface EvChargeMilestoneRule {
    fun check(item: EvChargeMilestone): MutableList<String>
}

data class EvChargeMilestoneNameLengthRule(var maxLength: Int) : EvChargeMilestoneRule {
    override fun check(item: EvChargeMilestone): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class EvChargeMilestoneFuelLitersRangeRule(var min: Double, var max: Double) : EvChargeMilestoneRule {
    override fun check(item: EvChargeMilestone): MutableList<String> {
        val value = item.fuelLiters
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("fuel liters $value is below $min")
        }
        if (value > max) {
            problems.add("fuel liters $value is above $max")
        }
        return problems
    }
}

data class EvChargeMilestoneCodePrefixRule(var prefix: String) : EvChargeMilestoneRule {
    override fun check(item: EvChargeMilestone): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class EvChargeMilestoneCodeLengthRule(var maxLength: Int) : EvChargeMilestoneRule {
    override fun check(item: EvChargeMilestone): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a ev charge milestone. */
data class EvChargeMilestoneValidator(var rules: MutableList<EvChargeMilestoneRule>) {
    fun validate(item: EvChargeMilestone): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: EvChargeMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<EvChargeMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultEvChargeMilestoneValidator(): EvChargeMilestoneValidator {
    return EvChargeMilestoneValidator(
        mutableListOf<EvChargeMilestoneRule>(
            EvChargeMilestoneNameLengthRule(1000),
            EvChargeMilestoneFuelLitersRangeRule(6.3, 505.7),
            EvChargeMilestoneCodePrefixRule("OPS"),
            EvChargeMilestoneCodeLengthRule(64),
        ),
    )
}
