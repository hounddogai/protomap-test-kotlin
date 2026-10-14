package com.acme.transport.safety.nearmiss.segment

import com.acme.transport.kit.text.normalize

/** A rule a global near miss request must pass. Check returns its problems, or none. */
interface GlobalNearMissRequestRule {
    fun check(item: GlobalNearMissRequest): MutableList<String>
}

data class GlobalNearMissRequestRevisionRangeRule(var min: Int, var max: Int) : GlobalNearMissRequestRule {
    override fun check(item: GlobalNearMissRequest): MutableList<String> {
        val value = item.revision
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("revision $value is below $min")
        }
        if (value > max) {
            problems.add("revision $value is above $max")
        }
        return problems
    }
}

data class GlobalNearMissRequestTitleLengthRule(var maxLength: Int) : GlobalNearMissRequestRule {
    override fun check(item: GlobalNearMissRequest): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalNearMissRequestDriverIdLengthRule(var maxLength: Int) : GlobalNearMissRequestRule {
    override fun check(item: GlobalNearMissRequest): MutableList<String> {
        val size = item.driverId.length
        if (size > maxLength) {
            return mutableListOf<String>("driver id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalNearMissRequestAllowedTitleRule(var allowed: MutableList<String>) : GlobalNearMissRequestRule {
    override fun check(item: GlobalNearMissRequest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class GlobalNearMissRequestVersionRangeRule(var min: Int, var max: Int) : GlobalNearMissRequestRule {
    override fun check(item: GlobalNearMissRequest): MutableList<String> {
        val value = item.version
        if (value < min || value > max) {
            return mutableListOf<String>("version $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class GlobalNearMissRequestRequiredDriverIdRule(var message: String) : GlobalNearMissRequestRule {
    override fun check(item: GlobalNearMissRequest): MutableList<String> {
        if (item.driverId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global near miss request. */
data class GlobalNearMissRequestValidator(var rules: MutableList<GlobalNearMissRequestRule>) {
    fun validate(item: GlobalNearMissRequest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: GlobalNearMissRequest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalNearMissRequest>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalNearMissRequestValidator(): GlobalNearMissRequestValidator {
    return GlobalNearMissRequestValidator(
        mutableListOf<GlobalNearMissRequestRule>(
            GlobalNearMissRequestRevisionRangeRule(255, 3600),
            GlobalNearMissRequestTitleLengthRule(16),
            GlobalNearMissRequestDriverIdLengthRule(120),
            GlobalNearMissRequestAllowedTitleRule(mutableListOf<String>("capped")),
            GlobalNearMissRequestVersionRangeRule(64, 64),
            GlobalNearMissRequestRequiredDriverIdRule("global near miss request needs an entry"),
        ),
    )
}
