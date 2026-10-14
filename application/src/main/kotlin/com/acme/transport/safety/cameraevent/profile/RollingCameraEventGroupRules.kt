package com.acme.transport.safety.cameraevent.profile

/** A rule a rolling camera event group must pass. Check returns its problems, or none. */
interface RollingCameraEventGroupRule {
    fun check(item: RollingCameraEventGroup): MutableList<String>
}

data class RollingCameraEventGroupCostRangeRule(
    var min: Double,
    var max: Double,
) : RollingCameraEventGroupRule {
    override fun check(item: RollingCameraEventGroup): MutableList<String> {
        val value = item.cost
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("cost $value is below $min")
        }
        if (value > max) {
            problems.add("cost $value is above $max")
        }
        return problems
    }
}

data class RollingCameraEventGroupCreatedAtRangeRule(var min: Int, var max: Int) : RollingCameraEventGroupRule {
    override fun check(item: RollingCameraEventGroup): MutableList<String> {
        val value = item.createdAt
        if (value < min || value > max) {
            return mutableListOf<String>("created at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RollingCameraEventGroupCodeLengthRule(var maxLength: Int) : RollingCameraEventGroupRule {
    override fun check(item: RollingCameraEventGroup): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingCameraEventGroupRequiredDockRule(var message: String) : RollingCameraEventGroupRule {
    override fun check(item: RollingCameraEventGroup): MutableList<String> {
        if (item.dock.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling camera event group. */
data class RollingCameraEventGroupValidator(var rules: MutableList<RollingCameraEventGroupRule>) {
    fun validate(item: RollingCameraEventGroup): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RollingCameraEventGroup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingCameraEventGroup>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingCameraEventGroupValidator(): RollingCameraEventGroupValidator {
    return RollingCameraEventGroupValidator(
        mutableListOf<RollingCameraEventGroupRule>(
            RollingCameraEventGroupCostRangeRule(7.8, 3026.8),
            RollingCameraEventGroupCreatedAtRangeRule(80, 255),
            RollingCameraEventGroupCodeLengthRule(80),
            RollingCameraEventGroupRequiredDockRule("rolling camera event group needs a value"),
        ),
    )
}
