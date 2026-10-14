package com.acme.transport.network.zone.group

/** A rule a capped zone group must pass. Check returns its problems, or none. */
interface CappedZoneGroupRule {
    fun check(item: CappedZoneGroup): MutableList<String>
}

data class CappedZoneGroupRequiredCodeRule(var message: String) : CappedZoneGroupRule {
    override fun check(item: CappedZoneGroup): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedZoneGroupRequiredNameRule(var message: String) : CappedZoneGroupRule {
    override fun check(item: CappedZoneGroup): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedZoneGroupCodeLengthRule(var maxLength: Int) : CappedZoneGroupRule {
    override fun check(item: CappedZoneGroup): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedZoneGroupEndsAtRangeRule(var min: Int, var max: Int) : CappedZoneGroupRule {
    override fun check(item: CappedZoneGroup): MutableList<String> {
        val value = item.endsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ends at $value is below $min")
        }
        if (value > max) {
            problems.add("ends at $value is above $max")
        }
        return problems
    }
}

data class CappedZoneGroupDockLengthRule(var maxLength: Int) : CappedZoneGroupRule {
    override fun check(item: CappedZoneGroup): MutableList<String> {
        val size = item.dock.length
        if (size > maxLength) {
            return mutableListOf<String>("dock is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped zone group. */
data class CappedZoneGroupValidator(var rules: MutableList<CappedZoneGroupRule>) {
    fun validate(item: CappedZoneGroup): MutableList<String> {
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

    fun requireValid(item: CappedZoneGroup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedZoneGroup>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedZoneGroupValidator(): CappedZoneGroupValidator {
    return CappedZoneGroupValidator(
        mutableListOf<CappedZoneGroupRule>(
            CappedZoneGroupRequiredCodeRule("capped zone group needs a value"),
            CappedZoneGroupRequiredNameRule("capped zone group needs an entry"),
            CappedZoneGroupCodeLengthRule(3600),
            CappedZoneGroupEndsAtRangeRule(64, 3600),
            CappedZoneGroupDockLengthRule(86400),
        ),
    )
}
