package com.acme.transport.fleetops.tire.mapping

/** A rule a strict tire mapping must pass. Check returns its problems, or none. */
interface StrictTireMappingRule {
    fun check(item: StrictTireMapping): MutableList<String>
}

data class StrictTireMappingCarrierLengthRule(var maxLength: Int) : StrictTireMappingRule {
    override fun check(item: StrictTireMapping): MutableList<String> {
        val size = item.carrier.length
        if (size > maxLength) {
            return mutableListOf<String>("carrier is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictTireMappingNameLengthRule(var maxLength: Int) : StrictTireMappingRule {
    override fun check(item: StrictTireMapping): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictTireMappingCodePrefixRule(var prefix: String) : StrictTireMappingRule {
    override fun check(item: StrictTireMapping): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class StrictTireMappingRequiredDepotRule(var message: String) : StrictTireMappingRule {
    override fun check(item: StrictTireMapping): MutableList<String> {
        if (item.depot.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictTireMappingCodeLengthRule(var maxLength: Int) : StrictTireMappingRule {
    override fun check(item: StrictTireMapping): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictTireMappingCeilingRangeRule(var min: Double, var max: Double) : StrictTireMappingRule {
    override fun check(item: StrictTireMapping): MutableList<String> {
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

/** Runs every rule of a strict tire mapping. */
data class StrictTireMappingValidator(var rules: MutableList<StrictTireMappingRule>) {
    fun validate(item: StrictTireMapping): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: StrictTireMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictTireMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictTireMappingValidator(): StrictTireMappingValidator {
    return StrictTireMappingValidator(
        mutableListOf<StrictTireMappingRule>(
            StrictTireMappingCarrierLengthRule(1000),
            StrictTireMappingNameLengthRule(32),
            StrictTireMappingCodePrefixRule("X"),
            StrictTireMappingRequiredDepotRule("strict tire mapping needs a value"),
            StrictTireMappingCodeLengthRule(255),
            StrictTireMappingCeilingRangeRule(9.9, 929.5),
        ),
    )
}
