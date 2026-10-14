package com.acme.transport.terminal.dwell.proposal

/** A rule a primary dwell record must pass. Check returns its problems, or none. */
interface PrimaryDwellRecordRule {
    fun check(item: PrimaryDwellRecord): MutableList<String>
}

data class PrimaryDwellRecordRevisionRangeRule(var min: Int, var max: Int) : PrimaryDwellRecordRule {
    override fun check(item: PrimaryDwellRecord): MutableList<String> {
        val value = item.revision
        if (value < min || value > max) {
            return mutableListOf<String>("revision $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryDwellRecordCodePrefixRule(var prefix: String) : PrimaryDwellRecordRule {
    override fun check(item: PrimaryDwellRecord): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryDwellRecordRequiredNameRule(var message: String) : PrimaryDwellRecordRule {
    override fun check(item: PrimaryDwellRecord): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryDwellRecordThresholdRangeRule(var min: Double, var max: Double) : PrimaryDwellRecordRule {
    override fun check(item: PrimaryDwellRecord): MutableList<String> {
        val value = item.threshold
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("threshold $value is below $min")
        }
        if (value > max) {
            problems.add("threshold $value is above $max")
        }
        return problems
    }
}

data class PrimaryDwellRecordRequiredCodeRule(var message: String) : PrimaryDwellRecordRule {
    override fun check(item: PrimaryDwellRecord): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryDwellRecordNameLengthRule(var maxLength: Int) : PrimaryDwellRecordRule {
    override fun check(item: PrimaryDwellRecord): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary dwell record. */
data class PrimaryDwellRecordValidator(var rules: MutableList<PrimaryDwellRecordRule>) {
    fun validate(item: PrimaryDwellRecord): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PrimaryDwellRecord) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryDwellRecord>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryDwellRecordValidator(): PrimaryDwellRecordValidator {
    return PrimaryDwellRecordValidator(
        mutableListOf<PrimaryDwellRecordRule>(
            PrimaryDwellRecordRevisionRangeRule(80, 255),
            PrimaryDwellRecordCodePrefixRule("OPS"),
            PrimaryDwellRecordRequiredNameRule("primary dwell record needs a value"),
            PrimaryDwellRecordThresholdRangeRule(0.1, 730.3),
            PrimaryDwellRecordRequiredCodeRule("primary dwell record needs this field"),
            PrimaryDwellRecordNameLengthRule(80),
        ),
    )
}
