package com.acme.transport.emissions.emissionfactor.exception

/** A rule a global emission factor request must pass. Check returns its problems, or none. */
interface GlobalEmissionFactorRequestRule {
    fun check(item: GlobalEmissionFactorRequest): MutableList<String>
}

data class GlobalEmissionFactorRequestCodeLengthRule(var maxLength: Int) : GlobalEmissionFactorRequestRule {
    override fun check(item: GlobalEmissionFactorRequest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalEmissionFactorRequestRequiredDescriptionRule(
    var message: String,
) : GlobalEmissionFactorRequestRule {
    override fun check(item: GlobalEmissionFactorRequest): MutableList<String> {
        if (item.description.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalEmissionFactorRequestRegionLengthRule(var maxLength: Int) : GlobalEmissionFactorRequestRule {
    override fun check(item: GlobalEmissionFactorRequest): MutableList<String> {
        val size = item.region.length
        if (size > maxLength) {
            return mutableListOf<String>("region is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalEmissionFactorRequestUpdatedAtRangeRule(
    var min: Int,
    var max: Int,
) : GlobalEmissionFactorRequestRule {
    override fun check(item: GlobalEmissionFactorRequest): MutableList<String> {
        val value = item.updatedAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("updated at $value is below $min")
        }
        if (value > max) {
            problems.add("updated at $value is above $max")
        }
        return problems
    }
}

data class GlobalEmissionFactorRequestCodePrefixRule(var prefix: String) : GlobalEmissionFactorRequestRule {
    override fun check(item: GlobalEmissionFactorRequest): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class GlobalEmissionFactorRequestDescriptionLengthRule(
    var maxLength: Int,
) : GlobalEmissionFactorRequestRule {
    override fun check(item: GlobalEmissionFactorRequest): MutableList<String> {
        val size = item.description.length
        if (size > maxLength) {
            return mutableListOf<String>("description is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global emission factor request. */
data class GlobalEmissionFactorRequestValidator(var rules: MutableList<GlobalEmissionFactorRequestRule>) {
    fun validate(item: GlobalEmissionFactorRequest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: GlobalEmissionFactorRequest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalEmissionFactorRequest>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalEmissionFactorRequestValidator(): GlobalEmissionFactorRequestValidator {
    return GlobalEmissionFactorRequestValidator(
        mutableListOf<GlobalEmissionFactorRequestRule>(
            GlobalEmissionFactorRequestCodeLengthRule(1000),
            GlobalEmissionFactorRequestRequiredDescriptionRule("global emission factor request needs an entry"),
            GlobalEmissionFactorRequestRegionLengthRule(255),
            GlobalEmissionFactorRequestUpdatedAtRangeRule(120, 3600),
            GlobalEmissionFactorRequestCodePrefixRule("Q"),
            GlobalEmissionFactorRequestDescriptionLengthRule(1000),
        ),
    )
}
