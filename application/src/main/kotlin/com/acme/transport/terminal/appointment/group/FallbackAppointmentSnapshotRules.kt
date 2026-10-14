package com.acme.transport.terminal.appointment.group

/** A rule a fallback appointment snapshot must pass. Check returns its problems, or none. */
interface FallbackAppointmentSnapshotRule {
    fun check(item: FallbackAppointmentSnapshot): MutableList<String>
}

data class FallbackAppointmentSnapshotCodeLengthRule(var maxLength: Int) : FallbackAppointmentSnapshotRule {
    override fun check(item: FallbackAppointmentSnapshot): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackAppointmentSnapshotAttemptsRangeRule(
    var min: Int,
    var max: Int,
) : FallbackAppointmentSnapshotRule {
    override fun check(item: FallbackAppointmentSnapshot): MutableList<String> {
        val value = item.attempts
        if (value < min || value > max) {
            return mutableListOf<String>("attempts $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FallbackAppointmentSnapshotRequiredLabelRule(var message: String) : FallbackAppointmentSnapshotRule {
    override fun check(item: FallbackAppointmentSnapshot): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackAppointmentSnapshotRequiredTitleRule(var message: String) : FallbackAppointmentSnapshotRule {
    override fun check(item: FallbackAppointmentSnapshot): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackAppointmentSnapshotRequiredCodeRule(var message: String) : FallbackAppointmentSnapshotRule {
    override fun check(item: FallbackAppointmentSnapshot): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback appointment snapshot. */
data class FallbackAppointmentSnapshotValidator(var rules: MutableList<FallbackAppointmentSnapshotRule>) {
    fun validate(item: FallbackAppointmentSnapshot): MutableList<String> {
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

    fun requireValid(item: FallbackAppointmentSnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackAppointmentSnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackAppointmentSnapshotValidator(): FallbackAppointmentSnapshotValidator {
    return FallbackAppointmentSnapshotValidator(
        mutableListOf<FallbackAppointmentSnapshotRule>(
            FallbackAppointmentSnapshotCodeLengthRule(3600),
            FallbackAppointmentSnapshotAttemptsRangeRule(16, 16),
            FallbackAppointmentSnapshotRequiredLabelRule("fallback appointment snapshot needs an entry"),
            FallbackAppointmentSnapshotRequiredTitleRule("fallback appointment snapshot needs an entry"),
            FallbackAppointmentSnapshotRequiredCodeRule("fallback appointment snapshot needs an entry"),
        ),
    )
}
