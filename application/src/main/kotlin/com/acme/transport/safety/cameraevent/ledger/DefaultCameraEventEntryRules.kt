package com.acme.transport.safety.cameraevent.ledger

/** A rule a default camera event entry must pass. Check returns its problems, or none. */
interface DefaultCameraEventEntryRule {
    fun check(item: DefaultCameraEventEntry): MutableList<String>
}

data class DefaultCameraEventEntryFloorValueRangeRule(
    var min: Double,
    var max: Double,
) : DefaultCameraEventEntryRule {
    override fun check(item: DefaultCameraEventEntry): MutableList<String> {
        val value = item.floorValue
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("floor value $value is below $min")
        }
        if (value > max) {
            problems.add("floor value $value is above $max")
        }
        return problems
    }
}

data class DefaultCameraEventEntryRequiredLabelRule(var message: String) : DefaultCameraEventEntryRule {
    override fun check(item: DefaultCameraEventEntry): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultCameraEventEntryRequiredChecksumRule(var message: String) : DefaultCameraEventEntryRule {
    override fun check(item: DefaultCameraEventEntry): MutableList<String> {
        if (item.checksum.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultCameraEventEntryLabelLengthRule(var maxLength: Int) : DefaultCameraEventEntryRule {
    override fun check(item: DefaultCameraEventEntry): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default camera event entry. */
data class DefaultCameraEventEntryValidator(var rules: MutableList<DefaultCameraEventEntryRule>) {
    fun validate(item: DefaultCameraEventEntry): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefaultCameraEventEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultCameraEventEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultCameraEventEntryValidator(): DefaultCameraEventEntryValidator {
    return DefaultCameraEventEntryValidator(
        mutableListOf<DefaultCameraEventEntryRule>(
            DefaultCameraEventEntryFloorValueRangeRule(1.0, 781.5),
            DefaultCameraEventEntryRequiredLabelRule("default camera event entry needs a value"),
            DefaultCameraEventEntryRequiredChecksumRule("default camera event entry needs this field"),
            DefaultCameraEventEntryLabelLengthRule(80),
        ),
    )
}
