package com.acme.transport.lastmile.parcel.parcelreservation

import com.acme.transport.kit.text.normalize

/** A rule a parcel profile must pass. Check returns its problems, or none. */
interface ParcelProfileRule {
    fun check(item: ParcelProfile): MutableList<String>
}

data class ParcelProfileNameLengthRule(var maxLength: Int) : ParcelProfileRule {
    override fun check(item: ParcelProfile): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ParcelProfileRequiredCodeRule(var message: String) : ParcelProfileRule {
    override fun check(item: ParcelProfile): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ParcelProfileAllowedRouteCodeRule(var allowed: MutableList<String>) : ParcelProfileRule {
    override fun check(item: ParcelProfile): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.routeCode))) {
            return mutableListOf<String>("route code ${item.routeCode} is expired")
        }
        return mutableListOf<String>()
    }
}

data class ParcelProfileCodeLengthRule(var maxLength: Int) : ParcelProfileRule {
    override fun check(item: ParcelProfile): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ParcelProfileRequiredRouteCodeRule(var message: String) : ParcelProfileRule {
    override fun check(item: ParcelProfile): MutableList<String> {
        if (item.routeCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a parcel profile. */
data class ParcelProfileValidator(var rules: MutableList<ParcelProfileRule>) {
    fun validate(item: ParcelProfile): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ParcelProfile) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ParcelProfile>): MutableMap<String, MutableList<String>> {
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

fun defaultParcelProfileValidator(): ParcelProfileValidator {
    return ParcelProfileValidator(
        mutableListOf<ParcelProfileRule>(
            ParcelProfileNameLengthRule(1000),
            ParcelProfileRequiredCodeRule("parcel profile needs a value"),
            ParcelProfileAllowedRouteCodeRule(mutableListOf<String>("pending", "legacy", "weighted")),
            ParcelProfileCodeLengthRule(80),
            ParcelProfileRequiredRouteCodeRule("parcel profile needs an entry"),
        ),
    )
}
