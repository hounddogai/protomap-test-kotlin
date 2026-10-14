package com.acme.transport.crewing.roster.digest

/** A rule a roster digest must pass. Check returns its problems, or none. */
interface RosterDigestRule {
    fun check(item: RosterDigest): MutableList<String>
}

data class RosterDigestDistanceKmRangeRule(var min: Double, var max: Double) : RosterDigestRule {
    override fun check(item: RosterDigest): MutableList<String> {
        val value = item.distanceKm
        if (value < min || value > max) {
            return mutableListOf<String>("distance km $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RosterDigestQuantityRangeRule(var min: Int, var max: Int) : RosterDigestRule {
    override fun check(item: RosterDigest): MutableList<String> {
        val value = item.quantity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("quantity $value is below $min")
        }
        if (value > max) {
            problems.add("quantity $value is above $max")
        }
        return problems
    }
}

data class RosterDigestRequiredCodeRule(var message: String) : RosterDigestRule {
    override fun check(item: RosterDigest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RosterDigestCodePrefixRule(var prefix: String) : RosterDigestRule {
    override fun check(item: RosterDigest): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RosterDigestRequiredNameRule(var message: String) : RosterDigestRule {
    override fun check(item: RosterDigest): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a roster digest. */
data class RosterDigestValidator(var rules: MutableList<RosterDigestRule>) {
    fun validate(item: RosterDigest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RosterDigest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RosterDigest>): MutableMap<String, MutableList<String>> {
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

fun defaultRosterDigestValidator(): RosterDigestValidator {
    return RosterDigestValidator(
        mutableListOf<RosterDigestRule>(
            RosterDigestDistanceKmRangeRule(2.3, 1460.5),
            RosterDigestQuantityRangeRule(16, 32),
            RosterDigestRequiredCodeRule("roster digest needs this field"),
            RosterDigestCodePrefixRule("X"),
            RosterDigestRequiredNameRule("roster digest needs this field"),
        ),
    )
}
