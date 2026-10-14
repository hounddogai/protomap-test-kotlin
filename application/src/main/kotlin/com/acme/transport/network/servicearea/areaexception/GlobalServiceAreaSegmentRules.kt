package com.acme.transport.network.servicearea.areaexception

/** A rule a global service area segment must pass. Check returns its problems, or none. */
interface GlobalServiceAreaSegmentRule {
    fun check(item: GlobalServiceAreaSegment): MutableList<String>
}

data class GlobalServiceAreaSegmentCodePrefixRule(var prefix: String) : GlobalServiceAreaSegmentRule {
    override fun check(item: GlobalServiceAreaSegment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class GlobalServiceAreaSegmentDepotLengthRule(var maxLength: Int) : GlobalServiceAreaSegmentRule {
    override fun check(item: GlobalServiceAreaSegment): MutableList<String> {
        val size = item.depot.length
        if (size > maxLength) {
            return mutableListOf<String>("depot is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalServiceAreaSegmentRequiredDepotRule(var message: String) : GlobalServiceAreaSegmentRule {
    override fun check(item: GlobalServiceAreaSegment): MutableList<String> {
        if (item.depot.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global service area segment. */
data class GlobalServiceAreaSegmentValidator(var rules: MutableList<GlobalServiceAreaSegmentRule>) {
    fun validate(item: GlobalServiceAreaSegment): MutableList<String> {
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

    fun requireValid(item: GlobalServiceAreaSegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalServiceAreaSegment>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalServiceAreaSegmentValidator(): GlobalServiceAreaSegmentValidator {
    return GlobalServiceAreaSegmentValidator(
        mutableListOf<GlobalServiceAreaSegmentRule>(
            GlobalServiceAreaSegmentCodePrefixRule("Q"),
            GlobalServiceAreaSegmentDepotLengthRule(86400),
            GlobalServiceAreaSegmentRequiredDepotRule("global service area segment needs a value"),
        ),
    )
}
