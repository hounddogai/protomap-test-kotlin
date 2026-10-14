package com.acme.transport.network.zone.group

/** A rule a zone profile must pass. Check returns its problems, or none. */
interface ZoneProfileRule {
    fun check(item: ZoneProfile): MutableList<String>
}

data class ZoneProfileRequiredTitleRule(var message: String) : ZoneProfileRule {
    override fun check(item: ZoneProfile): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ZoneProfileRequiredCodeRule(var message: String) : ZoneProfileRule {
    override fun check(item: ZoneProfile): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ZoneProfileTitleLengthRule(var maxLength: Int) : ZoneProfileRule {
    override fun check(item: ZoneProfile): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ZoneProfileCodePrefixRule(var prefix: String) : ZoneProfileRule {
    override fun check(item: ZoneProfile): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a zone profile. */
data class ZoneProfileValidator(var rules: MutableList<ZoneProfileRule>) {
    fun validate(item: ZoneProfile): MutableList<String> {
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

    fun requireValid(item: ZoneProfile) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ZoneProfile>): MutableMap<String, MutableList<String>> {
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

fun defaultZoneProfileValidator(): ZoneProfileValidator {
    return ZoneProfileValidator(
        mutableListOf<ZoneProfileRule>(
            ZoneProfileRequiredTitleRule("zone profile needs an entry"),
            ZoneProfileRequiredCodeRule("zone profile needs a value"),
            ZoneProfileTitleLengthRule(120),
            ZoneProfileCodePrefixRule("ACM"),
        ),
    )
}
