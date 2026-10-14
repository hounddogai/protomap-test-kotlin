package com.acme.transport.linehaul.relay.baseline

/** A rule a manual relay baseline must pass. Check returns its problems, or none. */
interface ManualRelayBaselineRule {
    fun check(item: ManualRelayBaseline): MutableList<String>
}

data class ManualRelayBaselineLaneLengthRule(var maxLength: Int) : ManualRelayBaselineRule {
    override fun check(item: ManualRelayBaseline): MutableList<String> {
        val size = item.lane.length
        if (size > maxLength) {
            return mutableListOf<String>("lane is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualRelayBaselineCodePrefixRule(var prefix: String) : ManualRelayBaselineRule {
    override fun check(item: ManualRelayBaseline): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ManualRelayBaselineRequiredCarrierRule(var message: String) : ManualRelayBaselineRule {
    override fun check(item: ManualRelayBaseline): MutableList<String> {
        if (item.carrier.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualRelayBaselineCodeLengthRule(var maxLength: Int) : ManualRelayBaselineRule {
    override fun check(item: ManualRelayBaseline): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualRelayBaselineWindowDaysRangeRule(var min: Int, var max: Int) : ManualRelayBaselineRule {
    override fun check(item: ManualRelayBaseline): MutableList<String> {
        val value = item.windowDays
        if (value < min || value > max) {
            return mutableListOf<String>("window days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual relay baseline. */
data class ManualRelayBaselineValidator(var rules: MutableList<ManualRelayBaselineRule>) {
    fun validate(item: ManualRelayBaseline): MutableList<String> {
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

    fun requireValid(item: ManualRelayBaseline) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualRelayBaseline>): MutableMap<String, MutableList<String>> {
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

fun defaultManualRelayBaselineValidator(): ManualRelayBaselineValidator {
    return ManualRelayBaselineValidator(
        mutableListOf<ManualRelayBaselineRule>(
            ManualRelayBaselineLaneLengthRule(64),
            ManualRelayBaselineCodePrefixRule("X"),
            ManualRelayBaselineRequiredCarrierRule("manual relay baseline needs a value"),
            ManualRelayBaselineCodeLengthRule(255),
            ManualRelayBaselineWindowDaysRangeRule(64, 1000),
        ),
    )
}
