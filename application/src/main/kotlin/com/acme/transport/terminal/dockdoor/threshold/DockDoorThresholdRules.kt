package com.acme.transport.terminal.dockdoor.threshold

/** A rule a dock door threshold must pass. Check returns its problems, or none. */
interface DockDoorThresholdRule {
    fun check(item: DockDoorThreshold): MutableList<String>
}

data class DockDoorThresholdRequiredRouteCodeRule(var message: String) : DockDoorThresholdRule {
    override fun check(item: DockDoorThreshold): MutableList<String> {
        if (item.routeCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DockDoorThresholdNameLengthRule(var maxLength: Int) : DockDoorThresholdRule {
    override fun check(item: DockDoorThreshold): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DockDoorThresholdRateRangeRule(var min: Double, var max: Double) : DockDoorThresholdRule {
    override fun check(item: DockDoorThreshold): MutableList<String> {
        val value = item.rate
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("rate $value is below $min")
        }
        if (value > max) {
            problems.add("rate $value is above $max")
        }
        return problems
    }
}

data class DockDoorThresholdRequiredNameRule(var message: String) : DockDoorThresholdRule {
    override fun check(item: DockDoorThreshold): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DockDoorThresholdRequiredPhoneRule(var message: String) : DockDoorThresholdRule {
    override fun check(item: DockDoorThreshold): MutableList<String> {
        if (item.phone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DockDoorThresholdRequiredCodeRule(var message: String) : DockDoorThresholdRule {
    override fun check(item: DockDoorThreshold): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a dock door threshold. */
data class DockDoorThresholdValidator(var rules: MutableList<DockDoorThresholdRule>) {
    fun validate(item: DockDoorThreshold): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DockDoorThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DockDoorThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultDockDoorThresholdValidator(): DockDoorThresholdValidator {
    return DockDoorThresholdValidator(
        mutableListOf<DockDoorThresholdRule>(
            DockDoorThresholdRequiredRouteCodeRule("dock door threshold needs a value"),
            DockDoorThresholdNameLengthRule(120),
            DockDoorThresholdRateRangeRule(9.0, 4358.2),
            DockDoorThresholdRequiredNameRule("dock door threshold needs an entry"),
            DockDoorThresholdRequiredPhoneRule("dock door threshold needs a value"),
            DockDoorThresholdRequiredCodeRule("dock door threshold needs this field"),
        ),
    )
}
