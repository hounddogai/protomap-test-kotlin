package com.acme.transport.network.zone.entry

/** A rule a global zone claim must pass. Check returns its problems, or none. */
interface GlobalZoneClaimRule {
    fun check(item: GlobalZoneClaim): MutableList<String>
}

data class GlobalZoneClaimSequenceRangeRule(var min: Int, var max: Int) : GlobalZoneClaimRule {
    override fun check(item: GlobalZoneClaim): MutableList<String> {
        val value = item.sequence
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("sequence $value is below $min")
        }
        if (value > max) {
            problems.add("sequence $value is above $max")
        }
        return problems
    }
}

data class GlobalZoneClaimNotesLengthRule(var maxLength: Int) : GlobalZoneClaimRule {
    override fun check(item: GlobalZoneClaim): MutableList<String> {
        val size = item.notes.length
        if (size > maxLength) {
            return mutableListOf<String>("notes is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalZoneClaimDurationMinutesRangeRule(var min: Int, var max: Int) : GlobalZoneClaimRule {
    override fun check(item: GlobalZoneClaim): MutableList<String> {
        val value = item.durationMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("duration minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class GlobalZoneClaimEtaMinutesRangeRule(var min: Int, var max: Int) : GlobalZoneClaimRule {
    override fun check(item: GlobalZoneClaim): MutableList<String> {
        val value = item.etaMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("eta minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global zone claim. */
data class GlobalZoneClaimValidator(var rules: MutableList<GlobalZoneClaimRule>) {
    fun validate(item: GlobalZoneClaim): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: GlobalZoneClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalZoneClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalZoneClaimValidator(): GlobalZoneClaimValidator {
    return GlobalZoneClaimValidator(
        mutableListOf<GlobalZoneClaimRule>(
            GlobalZoneClaimSequenceRangeRule(120, 255),
            GlobalZoneClaimNotesLengthRule(3600),
            GlobalZoneClaimDurationMinutesRangeRule(16, 120),
            GlobalZoneClaimEtaMinutesRangeRule(32, 120),
        ),
    )
}
