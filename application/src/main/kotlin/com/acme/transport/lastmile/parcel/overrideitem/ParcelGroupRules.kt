package com.acme.transport.lastmile.parcel.overrideitem

/** A rule a parcel group must pass. Check returns its problems, or none. */
interface ParcelGroupRule {
    fun check(item: ParcelGroup): MutableList<String>
}

data class ParcelGroupCodePrefixRule(var prefix: String) : ParcelGroupRule {
    override fun check(item: ParcelGroup): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ParcelGroupRequiredNameRule(var message: String) : ParcelGroupRule {
    override fun check(item: ParcelGroup): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ParcelGroupRouteCodeLengthRule(var maxLength: Int) : ParcelGroupRule {
    override fun check(item: ParcelGroup): MutableList<String> {
        val size = item.routeCode.length
        if (size > maxLength) {
            return mutableListOf<String>("route code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a parcel group. */
data class ParcelGroupValidator(var rules: MutableList<ParcelGroupRule>) {
    fun validate(item: ParcelGroup): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ParcelGroup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ParcelGroup>): MutableMap<String, MutableList<String>> {
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

fun defaultParcelGroupValidator(): ParcelGroupValidator {
    return ParcelGroupValidator(
        mutableListOf<ParcelGroupRule>(
            ParcelGroupCodePrefixRule("X"),
            ParcelGroupRequiredNameRule("parcel group needs this field"),
            ParcelGroupRouteCodeLengthRule(80),
        ),
    )
}
