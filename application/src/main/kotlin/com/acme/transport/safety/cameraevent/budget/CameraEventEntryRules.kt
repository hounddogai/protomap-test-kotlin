package com.acme.transport.safety.cameraevent.budget

/** A rule a camera event entry must pass. Check returns its problems, or none. */
interface CameraEventEntryRule {
    fun check(item: CameraEventEntry): MutableList<String>
}

data class CameraEventEntryDiscountRangeRule(var min: Double, var max: Double) : CameraEventEntryRule {
    override fun check(item: CameraEventEntry): MutableList<String> {
        val value = item.discount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("discount $value is below $min")
        }
        if (value > max) {
            problems.add("discount $value is above $max")
        }
        return problems
    }
}

data class CameraEventEntryRequiredCodeRule(var message: String) : CameraEventEntryRule {
    override fun check(item: CameraEventEntry): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CameraEventEntryUpdatedAtRangeRule(var min: Int, var max: Int) : CameraEventEntryRule {
    override fun check(item: CameraEventEntry): MutableList<String> {
        val value = item.updatedAt
        if (value < min || value > max) {
            return mutableListOf<String>("updated at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CameraEventEntryCodeLengthRule(var maxLength: Int) : CameraEventEntryRule {
    override fun check(item: CameraEventEntry): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a camera event entry. */
data class CameraEventEntryValidator(var rules: MutableList<CameraEventEntryRule>) {
    fun validate(item: CameraEventEntry): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CameraEventEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CameraEventEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultCameraEventEntryValidator(): CameraEventEntryValidator {
    return CameraEventEntryValidator(
        mutableListOf<CameraEventEntryRule>(
            CameraEventEntryDiscountRangeRule(3.3, 690.9),
            CameraEventEntryRequiredCodeRule("camera event entry needs this field"),
            CameraEventEntryUpdatedAtRangeRule(255, 1000),
            CameraEventEntryCodeLengthRule(80),
        ),
    )
}
