package com.acme.transport.emissions.evcharge.manifest

/** A rule a manual ev charge segment must pass. Check returns its problems, or none. */
interface ManualEvChargeSegmentRule {
    fun check(item: ManualEvChargeSegment): MutableList<String>
}

data class ManualEvChargeSegmentRetriesRangeRule(var min: Int, var max: Int) : ManualEvChargeSegmentRule {
    override fun check(item: ManualEvChargeSegment): MutableList<String> {
        val value = item.retries
        if (value < min || value > max) {
            return mutableListOf<String>("retries $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ManualEvChargeSegmentSourceLengthRule(var maxLength: Int) : ManualEvChargeSegmentRule {
    override fun check(item: ManualEvChargeSegment): MutableList<String> {
        val size = item.source.length
        if (size > maxLength) {
            return mutableListOf<String>("source is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualEvChargeSegmentRequiredNameRule(var message: String) : ManualEvChargeSegmentRule {
    override fun check(item: ManualEvChargeSegment): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualEvChargeSegmentCodeLengthRule(var maxLength: Int) : ManualEvChargeSegmentRule {
    override fun check(item: ManualEvChargeSegment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual ev charge segment. */
data class ManualEvChargeSegmentValidator(var rules: MutableList<ManualEvChargeSegmentRule>) {
    fun validate(item: ManualEvChargeSegment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ManualEvChargeSegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualEvChargeSegment>): MutableMap<String, MutableList<String>> {
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

fun defaultManualEvChargeSegmentValidator(): ManualEvChargeSegmentValidator {
    return ManualEvChargeSegmentValidator(
        mutableListOf<ManualEvChargeSegmentRule>(
            ManualEvChargeSegmentRetriesRangeRule(255, 86400),
            ManualEvChargeSegmentSourceLengthRule(16),
            ManualEvChargeSegmentRequiredNameRule("manual ev charge segment needs this field"),
            ManualEvChargeSegmentCodeLengthRule(32),
        ),
    )
}
