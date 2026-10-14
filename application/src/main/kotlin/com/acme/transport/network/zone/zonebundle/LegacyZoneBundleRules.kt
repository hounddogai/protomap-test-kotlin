package com.acme.transport.network.zone.zonebundle

/** A rule a legacy zone bundle must pass. Check returns its problems, or none. */
interface LegacyZoneBundleRule {
    fun check(item: LegacyZoneBundle): MutableList<String>
}

data class LegacyZoneBundleRequiredNameRule(var message: String) : LegacyZoneBundleRule {
    override fun check(item: LegacyZoneBundle): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyZoneBundleCodeLengthRule(var maxLength: Int) : LegacyZoneBundleRule {
    override fun check(item: LegacyZoneBundle): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyZoneBundleRequiredCodeRule(var message: String) : LegacyZoneBundleRule {
    override fun check(item: LegacyZoneBundle): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy zone bundle. */
data class LegacyZoneBundleValidator(var rules: MutableList<LegacyZoneBundleRule>) {
    fun validate(item: LegacyZoneBundle): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LegacyZoneBundle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyZoneBundle>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyZoneBundleValidator(): LegacyZoneBundleValidator {
    return LegacyZoneBundleValidator(
        mutableListOf<LegacyZoneBundleRule>(
            LegacyZoneBundleRequiredNameRule("legacy zone bundle needs this field"),
            LegacyZoneBundleCodeLengthRule(64),
            LegacyZoneBundleRequiredCodeRule("legacy zone bundle needs a value"),
        ),
    )
}
