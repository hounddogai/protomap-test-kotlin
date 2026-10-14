package com.acme.transport.customs.broker.request

/** A rule a scheduled broker request must pass. Check returns its problems, or none. */
interface ScheduledBrokerRequestRule {
    fun check(item: ScheduledBrokerRequest): MutableList<String>
}

data class ScheduledBrokerRequestRequiredTitleRule(var message: String) : ScheduledBrokerRequestRule {
    override fun check(item: ScheduledBrokerRequest): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledBrokerRequestNameLengthRule(var maxLength: Int) : ScheduledBrokerRequestRule {
    override fun check(item: ScheduledBrokerRequest): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledBrokerRequestRequiredCodeRule(var message: String) : ScheduledBrokerRequestRule {
    override fun check(item: ScheduledBrokerRequest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledBrokerRequestFloorValueRangeRule(
    var min: Double,
    var max: Double,
) : ScheduledBrokerRequestRule {
    override fun check(item: ScheduledBrokerRequest): MutableList<String> {
        val value = item.floorValue
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("floor value $value is below $min")
        }
        if (value > max) {
            problems.add("floor value $value is above $max")
        }
        return problems
    }
}

data class ScheduledBrokerRequestDistanceKmRangeRule(
    var min: Double,
    var max: Double,
) : ScheduledBrokerRequestRule {
    override fun check(item: ScheduledBrokerRequest): MutableList<String> {
        val value = item.distanceKm
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("distance km $value is below $min")
        }
        if (value > max) {
            problems.add("distance km $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a scheduled broker request. */
data class ScheduledBrokerRequestValidator(var rules: MutableList<ScheduledBrokerRequestRule>) {
    fun validate(item: ScheduledBrokerRequest): MutableList<String> {
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

    fun requireValid(item: ScheduledBrokerRequest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledBrokerRequest>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledBrokerRequestValidator(): ScheduledBrokerRequestValidator {
    return ScheduledBrokerRequestValidator(
        mutableListOf<ScheduledBrokerRequestRule>(
            ScheduledBrokerRequestRequiredTitleRule("scheduled broker request needs a value"),
            ScheduledBrokerRequestNameLengthRule(255),
            ScheduledBrokerRequestRequiredCodeRule("scheduled broker request needs an entry"),
            ScheduledBrokerRequestFloorValueRangeRule(0.2, 4683.9),
            ScheduledBrokerRequestDistanceKmRangeRule(0.7, 1204.4),
        ),
    )
}
