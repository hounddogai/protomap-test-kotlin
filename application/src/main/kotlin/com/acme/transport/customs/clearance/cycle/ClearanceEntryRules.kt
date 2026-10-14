package com.acme.transport.customs.clearance.cycle

/** A rule a clearance entry must pass. Check returns its problems, or none. */
interface ClearanceEntryRule {
    fun check(item: ClearanceEntry): MutableList<String>
}

data class ClearanceEntryRequiredChecksumRule(var message: String) : ClearanceEntryRule {
    override fun check(item: ClearanceEntry): MutableList<String> {
        if (item.checksum.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ClearanceEntryRequiredCodeRule(var message: String) : ClearanceEntryRule {
    override fun check(item: ClearanceEntry): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ClearanceEntryRequiredNameRule(var message: String) : ClearanceEntryRule {
    override fun check(item: ClearanceEntry): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ClearanceEntryLaneLengthRule(var maxLength: Int) : ClearanceEntryRule {
    override fun check(item: ClearanceEntry): MutableList<String> {
        val size = item.lane.length
        if (size > maxLength) {
            return mutableListOf<String>("lane is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ClearanceEntryChecksumLengthRule(var maxLength: Int) : ClearanceEntryRule {
    override fun check(item: ClearanceEntry): MutableList<String> {
        val size = item.checksum.length
        if (size > maxLength) {
            return mutableListOf<String>("checksum is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ClearanceEntryCodeLengthRule(var maxLength: Int) : ClearanceEntryRule {
    override fun check(item: ClearanceEntry): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a clearance entry. */
data class ClearanceEntryValidator(var rules: MutableList<ClearanceEntryRule>) {
    fun validate(item: ClearanceEntry): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ClearanceEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ClearanceEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultClearanceEntryValidator(): ClearanceEntryValidator {
    return ClearanceEntryValidator(
        mutableListOf<ClearanceEntryRule>(
            ClearanceEntryRequiredChecksumRule("clearance entry needs an entry"),
            ClearanceEntryRequiredCodeRule("clearance entry needs a value"),
            ClearanceEntryRequiredNameRule("clearance entry needs this field"),
            ClearanceEntryLaneLengthRule(120),
            ClearanceEntryChecksumLengthRule(32),
            ClearanceEntryCodeLengthRule(86400),
        ),
    )
}
