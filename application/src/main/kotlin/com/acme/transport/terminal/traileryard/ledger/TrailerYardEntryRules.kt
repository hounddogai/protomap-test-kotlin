package com.acme.transport.terminal.traileryard.ledger

/** A rule a trailer yard entry must pass. Check returns its problems, or none. */
interface TrailerYardEntryRule {
    fun check(item: TrailerYardEntry): MutableList<String>
}

data class TrailerYardEntryScoreRangeRule(var min: Double, var max: Double) : TrailerYardEntryRule {
    override fun check(item: TrailerYardEntry): MutableList<String> {
        val value = item.score
        if (value < min || value > max) {
            return mutableListOf<String>("score $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TrailerYardEntryCodeLengthRule(var maxLength: Int) : TrailerYardEntryRule {
    override fun check(item: TrailerYardEntry): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TrailerYardEntryTrailerIdLengthRule(var maxLength: Int) : TrailerYardEntryRule {
    override fun check(item: TrailerYardEntry): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TrailerYardEntryRevisionRangeRule(var min: Int, var max: Int) : TrailerYardEntryRule {
    override fun check(item: TrailerYardEntry): MutableList<String> {
        val value = item.revision
        if (value < min || value > max) {
            return mutableListOf<String>("revision $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TrailerYardEntryEtaMinutesRangeRule(var min: Int, var max: Int) : TrailerYardEntryRule {
    override fun check(item: TrailerYardEntry): MutableList<String> {
        val value = item.etaMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("eta minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a trailer yard entry. */
data class TrailerYardEntryValidator(var rules: MutableList<TrailerYardEntryRule>) {
    fun validate(item: TrailerYardEntry): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TrailerYardEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TrailerYardEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultTrailerYardEntryValidator(): TrailerYardEntryValidator {
    return TrailerYardEntryValidator(
        mutableListOf<TrailerYardEntryRule>(
            TrailerYardEntryScoreRangeRule(3.5, 4384.7),
            TrailerYardEntryCodeLengthRule(80),
            TrailerYardEntryTrailerIdLengthRule(120),
            TrailerYardEntryRevisionRangeRule(32, 255),
            TrailerYardEntryEtaMinutesRangeRule(64, 3600),
        ),
    )
}
