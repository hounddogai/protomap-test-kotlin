package com.acme.transport.emissions.evcharge.template

/** A rule a default ev charge group must pass. Check returns its problems, or none. */
interface DefaultEvChargeGroupRule {
    fun check(item: DefaultEvChargeGroup): MutableList<String>
}

data class DefaultEvChargeGroupDriverIdLengthRule(var maxLength: Int) : DefaultEvChargeGroupRule {
    override fun check(item: DefaultEvChargeGroup): MutableList<String> {
        val size = item.driverId.length
        if (size > maxLength) {
            return mutableListOf<String>("driver id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultEvChargeGroupRequiredDriverIdRule(var message: String) : DefaultEvChargeGroupRule {
    override fun check(item: DefaultEvChargeGroup): MutableList<String> {
        if (item.driverId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultEvChargeGroupTitleLengthRule(var maxLength: Int) : DefaultEvChargeGroupRule {
    override fun check(item: DefaultEvChargeGroup): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default ev charge group. */
data class DefaultEvChargeGroupValidator(var rules: MutableList<DefaultEvChargeGroupRule>) {
    fun validate(item: DefaultEvChargeGroup): MutableList<String> {
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

    fun requireValid(item: DefaultEvChargeGroup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultEvChargeGroup>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultEvChargeGroupValidator(): DefaultEvChargeGroupValidator {
    return DefaultEvChargeGroupValidator(
        mutableListOf<DefaultEvChargeGroupRule>(
            DefaultEvChargeGroupDriverIdLengthRule(1000),
            DefaultEvChargeGroupRequiredDriverIdRule("default ev charge group needs an entry"),
            DefaultEvChargeGroupTitleLengthRule(120),
        ),
    )
}
