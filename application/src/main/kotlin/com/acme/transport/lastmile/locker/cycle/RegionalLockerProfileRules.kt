package com.acme.transport.lastmile.locker.cycle

/** A rule a regional locker profile must pass. Check returns its problems, or none. */
interface RegionalLockerProfileRule {
    fun check(item: RegionalLockerProfile): MutableList<String>
}

data class RegionalLockerProfileRequiredNameRule(var message: String) : RegionalLockerProfileRule {
    override fun check(item: RegionalLockerProfile): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalLockerProfilePriorityRangeRule(var min: Int, var max: Int) : RegionalLockerProfileRule {
    override fun check(item: RegionalLockerProfile): MutableList<String> {
        val value = item.priority
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("priority $value is below $min")
        }
        if (value > max) {
            problems.add("priority $value is above $max")
        }
        return problems
    }
}

data class RegionalLockerProfileCodeLengthRule(var maxLength: Int) : RegionalLockerProfileRule {
    override fun check(item: RegionalLockerProfile): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalLockerProfileDescriptionLengthRule(var maxLength: Int) : RegionalLockerProfileRule {
    override fun check(item: RegionalLockerProfile): MutableList<String> {
        val size = item.description.length
        if (size > maxLength) {
            return mutableListOf<String>("description is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalLockerProfileNameLengthRule(var maxLength: Int) : RegionalLockerProfileRule {
    override fun check(item: RegionalLockerProfile): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional locker profile. */
data class RegionalLockerProfileValidator(var rules: MutableList<RegionalLockerProfileRule>) {
    fun validate(item: RegionalLockerProfile): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RegionalLockerProfile) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalLockerProfile>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalLockerProfileValidator(): RegionalLockerProfileValidator {
    return RegionalLockerProfileValidator(
        mutableListOf<RegionalLockerProfileRule>(
            RegionalLockerProfileRequiredNameRule("regional locker profile needs a value"),
            RegionalLockerProfilePriorityRangeRule(120, 86400),
            RegionalLockerProfileCodeLengthRule(255),
            RegionalLockerProfileDescriptionLengthRule(255),
            RegionalLockerProfileNameLengthRule(32),
        ),
    )
}
