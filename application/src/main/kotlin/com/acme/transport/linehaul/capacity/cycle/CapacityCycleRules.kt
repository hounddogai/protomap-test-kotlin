package com.acme.transport.linehaul.capacity.cycle

/** A rule a capacity cycle must pass. Check returns its problems, or none. */
interface CapacityCycleRule {
    fun check(item: CapacityCycle): MutableList<String>
}

data class CapacityCycleRequiredTrailerIdRule(var message: String) : CapacityCycleRule {
    override fun check(item: CapacityCycle): MutableList<String> {
        if (item.trailerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CapacityCycleTrailerIdLengthRule(var maxLength: Int) : CapacityCycleRule {
    override fun check(item: CapacityCycle): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CapacityCycleMarginRangeRule(var min: Double, var max: Double) : CapacityCycleRule {
    override fun check(item: CapacityCycle): MutableList<String> {
        val value = item.margin
        if (value < min || value > max) {
            return mutableListOf<String>("margin $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CapacityCycleExpiresAtRangeRule(var min: Int, var max: Int) : CapacityCycleRule {
    override fun check(item: CapacityCycle): MutableList<String> {
        val value = item.expiresAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("expires at $value is below $min")
        }
        if (value > max) {
            problems.add("expires at $value is above $max")
        }
        return problems
    }
}

data class CapacityCyclePhoneLengthRule(var maxLength: Int) : CapacityCycleRule {
    override fun check(item: CapacityCycle): MutableList<String> {
        val size = item.phone.length
        if (size > maxLength) {
            return mutableListOf<String>("phone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CapacityCycleUpdatedAtRangeRule(var min: Int, var max: Int) : CapacityCycleRule {
    override fun check(item: CapacityCycle): MutableList<String> {
        val value = item.updatedAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("updated at $value is below $min")
        }
        if (value > max) {
            problems.add("updated at $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a capacity cycle. */
data class CapacityCycleValidator(var rules: MutableList<CapacityCycleRule>) {
    fun validate(item: CapacityCycle): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CapacityCycle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CapacityCycle>): MutableMap<String, MutableList<String>> {
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

fun defaultCapacityCycleValidator(): CapacityCycleValidator {
    return CapacityCycleValidator(
        mutableListOf<CapacityCycleRule>(
            CapacityCycleRequiredTrailerIdRule("capacity cycle needs a value"),
            CapacityCycleTrailerIdLengthRule(80),
            CapacityCycleMarginRangeRule(7.2, 4429.3),
            CapacityCycleExpiresAtRangeRule(80, 3600),
            CapacityCyclePhoneLengthRule(32),
            CapacityCycleUpdatedAtRangeRule(32, 86400),
        ),
    )
}
