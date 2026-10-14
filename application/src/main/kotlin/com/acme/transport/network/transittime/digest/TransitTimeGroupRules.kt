package com.acme.transport.network.transittime.digest

/** A rule a transit time group must pass. Check returns its problems, or none. */
interface TransitTimeGroupRule {
    fun check(item: TransitTimeGroup): MutableList<String>
}

data class TransitTimeGroupCodeLengthRule(var maxLength: Int) : TransitTimeGroupRule {
    override fun check(item: TransitTimeGroup): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TransitTimeGroupRequiredTrailerIdRule(var message: String) : TransitTimeGroupRule {
    override fun check(item: TransitTimeGroup): MutableList<String> {
        if (item.trailerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TransitTimeGroupRequiredNameRule(var message: String) : TransitTimeGroupRule {
    override fun check(item: TransitTimeGroup): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TransitTimeGroupCodePrefixRule(var prefix: String) : TransitTimeGroupRule {
    override fun check(item: TransitTimeGroup): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a transit time group. */
data class TransitTimeGroupValidator(var rules: MutableList<TransitTimeGroupRule>) {
    fun validate(item: TransitTimeGroup): MutableList<String> {
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

    fun requireValid(item: TransitTimeGroup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TransitTimeGroup>): MutableMap<String, MutableList<String>> {
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

fun defaultTransitTimeGroupValidator(): TransitTimeGroupValidator {
    return TransitTimeGroupValidator(
        mutableListOf<TransitTimeGroupRule>(
            TransitTimeGroupCodeLengthRule(3600),
            TransitTimeGroupRequiredTrailerIdRule("transit time group needs this field"),
            TransitTimeGroupRequiredNameRule("transit time group needs a value"),
            TransitTimeGroupCodePrefixRule("OPS"),
        ),
    )
}
