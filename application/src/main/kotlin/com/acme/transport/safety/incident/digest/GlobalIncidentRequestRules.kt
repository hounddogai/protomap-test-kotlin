package com.acme.transport.safety.incident.digest

/** A rule a global incident request must pass. Check returns its problems, or none. */
interface GlobalIncidentRequestRule {
    fun check(item: GlobalIncidentRequest): MutableList<String>
}

data class GlobalIncidentRequestCreatedAtRangeRule(var min: Int, var max: Int) : GlobalIncidentRequestRule {
    override fun check(item: GlobalIncidentRequest): MutableList<String> {
        val value = item.createdAt
        if (value < min || value > max) {
            return mutableListOf<String>("created at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class GlobalIncidentRequestRequiredOwnerIdRule(var message: String) : GlobalIncidentRequestRule {
    override fun check(item: GlobalIncidentRequest): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalIncidentRequestTitleLengthRule(var maxLength: Int) : GlobalIncidentRequestRule {
    override fun check(item: GlobalIncidentRequest): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalIncidentRequestEtaMinutesRangeRule(var min: Int, var max: Int) : GlobalIncidentRequestRule {
    override fun check(item: GlobalIncidentRequest): MutableList<String> {
        val value = item.etaMinutes
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("eta minutes $value is below $min")
        }
        if (value > max) {
            problems.add("eta minutes $value is above $max")
        }
        return problems
    }
}

data class GlobalIncidentRequestCodePrefixRule(var prefix: String) : GlobalIncidentRequestRule {
    override fun check(item: GlobalIncidentRequest): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class GlobalIncidentRequestDriverIdLengthRule(var maxLength: Int) : GlobalIncidentRequestRule {
    override fun check(item: GlobalIncidentRequest): MutableList<String> {
        val size = item.driverId.length
        if (size > maxLength) {
            return mutableListOf<String>("driver id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global incident request. */
data class GlobalIncidentRequestValidator(var rules: MutableList<GlobalIncidentRequestRule>) {
    fun validate(item: GlobalIncidentRequest): MutableList<String> {
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

    fun requireValid(item: GlobalIncidentRequest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalIncidentRequest>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalIncidentRequestValidator(): GlobalIncidentRequestValidator {
    return GlobalIncidentRequestValidator(
        mutableListOf<GlobalIncidentRequestRule>(
            GlobalIncidentRequestCreatedAtRangeRule(64, 3600),
            GlobalIncidentRequestRequiredOwnerIdRule("global incident request needs this field"),
            GlobalIncidentRequestTitleLengthRule(32),
            GlobalIncidentRequestEtaMinutesRangeRule(16, 86400),
            GlobalIncidentRequestCodePrefixRule("OPS"),
            GlobalIncidentRequestDriverIdLengthRule(64),
        ),
    )
}
