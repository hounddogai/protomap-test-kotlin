package com.acme.transport.emissions.fuelburn.proposal

/** A rule a manual fuel burn digest must pass. Check returns its problems, or none. */
interface ManualFuelBurnDigestRule {
    fun check(item: ManualFuelBurnDigest): MutableList<String>
}

data class ManualFuelBurnDigestRequiredCodeRule(var message: String) : ManualFuelBurnDigestRule {
    override fun check(item: ManualFuelBurnDigest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualFuelBurnDigestCodePrefixRule(var prefix: String) : ManualFuelBurnDigestRule {
    override fun check(item: ManualFuelBurnDigest): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ManualFuelBurnDigestCeilingRangeRule(var min: Double, var max: Double) : ManualFuelBurnDigestRule {
    override fun check(item: ManualFuelBurnDigest): MutableList<String> {
        val value = item.ceiling
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ceiling $value is below $min")
        }
        if (value > max) {
            problems.add("ceiling $value is above $max")
        }
        return problems
    }
}

data class ManualFuelBurnDigestPriorityRangeRule(var min: Int, var max: Int) : ManualFuelBurnDigestRule {
    override fun check(item: ManualFuelBurnDigest): MutableList<String> {
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

/** Runs every rule of a manual fuel burn digest. */
data class ManualFuelBurnDigestValidator(var rules: MutableList<ManualFuelBurnDigestRule>) {
    fun validate(item: ManualFuelBurnDigest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ManualFuelBurnDigest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualFuelBurnDigest>): MutableMap<String, MutableList<String>> {
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

fun defaultManualFuelBurnDigestValidator(): ManualFuelBurnDigestValidator {
    return ManualFuelBurnDigestValidator(
        mutableListOf<ManualFuelBurnDigestRule>(
            ManualFuelBurnDigestRequiredCodeRule("manual fuel burn digest needs this field"),
            ManualFuelBurnDigestCodePrefixRule("ACM"),
            ManualFuelBurnDigestCeilingRangeRule(1.9, 338.6),
            ManualFuelBurnDigestPriorityRangeRule(120, 86400),
        ),
    )
}
