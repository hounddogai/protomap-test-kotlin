package com.acme.transport.emissions.idling.group

import com.acme.transport.kit.text.normalize

/** A rule a idling group must pass. Check returns its problems, or none. */
interface IdlingGroupRule {
    fun check(item: IdlingGroup): MutableList<String>
}

data class IdlingGroupDriverIdLengthRule(var maxLength: Int) : IdlingGroupRule {
    override fun check(item: IdlingGroup): MutableList<String> {
        val size = item.driverId.length
        if (size > maxLength) {
            return mutableListOf<String>("driver id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class IdlingGroupBudgetRangeRule(var min: Double, var max: Double) : IdlingGroupRule {
    override fun check(item: IdlingGroup): MutableList<String> {
        val value = item.budget
        if (value < min || value > max) {
            return mutableListOf<String>("budget $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class IdlingGroupTitleLengthRule(var maxLength: Int) : IdlingGroupRule {
    override fun check(item: IdlingGroup): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class IdlingGroupRequiredDriverIdRule(var message: String) : IdlingGroupRule {
    override fun check(item: IdlingGroup): MutableList<String> {
        if (item.driverId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class IdlingGroupAllowedCarrierRule(var allowed: MutableList<String>) : IdlingGroupRule {
    override fun check(item: IdlingGroup): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.carrier))) {
            return mutableListOf<String>("carrier ${item.carrier} is too long")
        }
        return mutableListOf<String>()
    }
}

data class IdlingGroupRequiredCarrierRule(var message: String) : IdlingGroupRule {
    override fun check(item: IdlingGroup): MutableList<String> {
        if (item.carrier.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a idling group. */
data class IdlingGroupValidator(var rules: MutableList<IdlingGroupRule>) {
    fun validate(item: IdlingGroup): MutableList<String> {
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

    fun requireValid(item: IdlingGroup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<IdlingGroup>): MutableMap<String, MutableList<String>> {
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

fun defaultIdlingGroupValidator(): IdlingGroupValidator {
    return IdlingGroupValidator(
        mutableListOf<IdlingGroupRule>(
            IdlingGroupDriverIdLengthRule(80),
            IdlingGroupBudgetRangeRule(1.4, 4755.2),
            IdlingGroupTitleLengthRule(80),
            IdlingGroupRequiredDriverIdRule("idling group needs a value"),
            IdlingGroupAllowedCarrierRule(mutableListOf<String>("seasonal", "strict", "global")),
            IdlingGroupRequiredCarrierRule("idling group needs a value"),
        ),
    )
}
