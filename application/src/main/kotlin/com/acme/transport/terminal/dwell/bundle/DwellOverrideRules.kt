package com.acme.transport.terminal.dwell.bundle

/** A rule a dwell override must pass. Check returns its problems, or none. */
interface DwellOverrideRule {
    fun check(item: DwellOverride): MutableList<String>
}

data class DwellOverrideEtaMinutesRangeRule(var min: Int, var max: Int) : DwellOverrideRule {
    override fun check(item: DwellOverride): MutableList<String> {
        val value = item.etaMinutes
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("eta minutes $value is below $min")
        }
        if (value > max) {
            problems.add("eta minutes $value is above $max")
        }
        return problems
    }
}

data class DwellOverrideRequiredDriverIdRule(var message: String) : DwellOverrideRule {
    override fun check(item: DwellOverride): MutableList<String> {
        if (item.driverId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DwellOverrideCodeLengthRule(var maxLength: Int) : DwellOverrideRule {
    override fun check(item: DwellOverride): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DwellOverrideDriverIdLengthRule(var maxLength: Int) : DwellOverrideRule {
    override fun check(item: DwellOverride): MutableList<String> {
        val size = item.driverId.length
        if (size > maxLength) {
            return mutableListOf<String>("driver id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DwellOverrideRequiredCodeRule(var message: String) : DwellOverrideRule {
    override fun check(item: DwellOverride): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a dwell override. */
data class DwellOverrideValidator(var rules: MutableList<DwellOverrideRule>) {
    fun validate(item: DwellOverride): MutableList<String> {
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

    fun requireValid(item: DwellOverride) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DwellOverride>): MutableMap<String, MutableList<String>> {
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

fun defaultDwellOverrideValidator(): DwellOverrideValidator {
    return DwellOverrideValidator(
        mutableListOf<DwellOverrideRule>(
            DwellOverrideEtaMinutesRangeRule(32, 32),
            DwellOverrideRequiredDriverIdRule("dwell override needs an entry"),
            DwellOverrideCodeLengthRule(120),
            DwellOverrideDriverIdLengthRule(32),
            DwellOverrideRequiredCodeRule("dwell override needs a value"),
        ),
    )
}
