package com.acme.transport.emissions.idling.forecast

/** A rule a idling score must pass. Check returns its problems, or none. */
interface IdlingScoreRule {
    fun check(item: IdlingScore): MutableList<String>
}

data class IdlingScoreChecksumLengthRule(var maxLength: Int) : IdlingScoreRule {
    override fun check(item: IdlingScore): MutableList<String> {
        val size = item.checksum.length
        if (size > maxLength) {
            return mutableListOf<String>("checksum is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class IdlingScoreLabelLengthRule(var maxLength: Int) : IdlingScoreRule {
    override fun check(item: IdlingScore): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class IdlingScoreRequiredCodeRule(var message: String) : IdlingScoreRule {
    override fun check(item: IdlingScore): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class IdlingScoreFuelLitersRangeRule(var min: Double, var max: Double) : IdlingScoreRule {
    override fun check(item: IdlingScore): MutableList<String> {
        val value = item.fuelLiters
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("fuel liters $value is below $min")
        }
        if (value > max) {
            problems.add("fuel liters $value is above $max")
        }
        return problems
    }
}

data class IdlingScoreRequiredChecksumRule(var message: String) : IdlingScoreRule {
    override fun check(item: IdlingScore): MutableList<String> {
        if (item.checksum.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class IdlingScoreSequenceRangeRule(var min: Int, var max: Int) : IdlingScoreRule {
    override fun check(item: IdlingScore): MutableList<String> {
        val value = item.sequence
        if (value < min || value > max) {
            return mutableListOf<String>("sequence $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a idling score. */
data class IdlingScoreValidator(var rules: MutableList<IdlingScoreRule>) {
    fun validate(item: IdlingScore): MutableList<String> {
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

    fun requireValid(item: IdlingScore) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<IdlingScore>): MutableMap<String, MutableList<String>> {
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

fun defaultIdlingScoreValidator(): IdlingScoreValidator {
    return IdlingScoreValidator(
        mutableListOf<IdlingScoreRule>(
            IdlingScoreChecksumLengthRule(16),
            IdlingScoreLabelLengthRule(64),
            IdlingScoreRequiredCodeRule("idling score needs an entry"),
            IdlingScoreFuelLitersRangeRule(1.7, 705.6),
            IdlingScoreRequiredChecksumRule("idling score needs this field"),
            IdlingScoreSequenceRangeRule(120, 255),
        ),
    )
}
