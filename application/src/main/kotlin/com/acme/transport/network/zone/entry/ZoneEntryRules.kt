package com.acme.transport.network.zone.entry

/** A rule a zone entry must pass. Check returns its problems, or none. */
interface ZoneEntryRule {
    fun check(item: ZoneEntry): MutableList<String>
}

data class ZoneEntryCodePrefixRule(var prefix: String) : ZoneEntryRule {
    override fun check(item: ZoneEntry): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ZoneEntryPhoneLengthRule(var maxLength: Int) : ZoneEntryRule {
    override fun check(item: ZoneEntry): MutableList<String> {
        val size = item.phone.length
        if (size > maxLength) {
            return mutableListOf<String>("phone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ZoneEntryTitleLengthRule(var maxLength: Int) : ZoneEntryRule {
    override fun check(item: ZoneEntry): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a zone entry. */
data class ZoneEntryValidator(var rules: MutableList<ZoneEntryRule>) {
    fun validate(item: ZoneEntry): MutableList<String> {
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

    fun requireValid(item: ZoneEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ZoneEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultZoneEntryValidator(): ZoneEntryValidator {
    return ZoneEntryValidator(
        mutableListOf<ZoneEntryRule>(
            ZoneEntryCodePrefixRule("OPS"),
            ZoneEntryPhoneLengthRule(80),
            ZoneEntryTitleLengthRule(16),
        ),
    )
}
