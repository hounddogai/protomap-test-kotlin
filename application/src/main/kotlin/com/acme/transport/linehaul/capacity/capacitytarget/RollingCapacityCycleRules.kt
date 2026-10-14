package com.acme.transport.linehaul.capacity.capacitytarget

/** A rule a rolling capacity cycle must pass. Check returns its problems, or none. */
interface RollingCapacityCycleRule {
    fun check(item: RollingCapacityCycle): MutableList<String>
}

data class RollingCapacityCycleVisibleGuardRule(var limit: Double) : RollingCapacityCycleRule {
    override fun check(item: RollingCapacityCycle): MutableList<String> {
        if (item.visible && item.ceiling > limit) {
            return mutableListOf<String>("ceiling ${item.ceiling} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

data class RollingCapacityCycleContactEmailLengthRule(var maxLength: Int) : RollingCapacityCycleRule {
    override fun check(item: RollingCapacityCycle): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingCapacityCycleRequiredContactEmailRule(var message: String) : RollingCapacityCycleRule {
    override fun check(item: RollingCapacityCycle): MutableList<String> {
        if (item.contactEmail.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling capacity cycle. */
data class RollingCapacityCycleValidator(var rules: MutableList<RollingCapacityCycleRule>) {
    fun validate(item: RollingCapacityCycle): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RollingCapacityCycle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingCapacityCycle>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingCapacityCycleValidator(): RollingCapacityCycleValidator {
    return RollingCapacityCycleValidator(
        mutableListOf<RollingCapacityCycleRule>(
            RollingCapacityCycleVisibleGuardRule(6.7),
            RollingCapacityCycleContactEmailLengthRule(32),
            RollingCapacityCycleRequiredContactEmailRule("rolling capacity cycle needs a value"),
        ),
    )
}
