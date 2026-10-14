package com.acme.transport.terminal.dockdoor.doorclaim

/** A rule a regional dock door claim must pass. Check returns its problems, or none. */
interface RegionalDockDoorClaimRule {
    fun check(item: RegionalDockDoorClaim): MutableList<String>
}

data class RegionalDockDoorClaimRequiredTimezoneRule(var message: String) : RegionalDockDoorClaimRule {
    override fun check(item: RegionalDockDoorClaim): MutableList<String> {
        if (item.timezone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalDockDoorClaimRetriesRangeRule(var min: Int, var max: Int) : RegionalDockDoorClaimRule {
    override fun check(item: RegionalDockDoorClaim): MutableList<String> {
        val value = item.retries
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("retries $value is below $min")
        }
        if (value > max) {
            problems.add("retries $value is above $max")
        }
        return problems
    }
}

data class RegionalDockDoorClaimRequiredNameRule(var message: String) : RegionalDockDoorClaimRule {
    override fun check(item: RegionalDockDoorClaim): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalDockDoorClaimNotesLengthRule(var maxLength: Int) : RegionalDockDoorClaimRule {
    override fun check(item: RegionalDockDoorClaim): MutableList<String> {
        val size = item.notes.length
        if (size > maxLength) {
            return mutableListOf<String>("notes is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalDockDoorClaimRequiredNotesRule(var message: String) : RegionalDockDoorClaimRule {
    override fun check(item: RegionalDockDoorClaim): MutableList<String> {
        if (item.notes.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional dock door claim. */
data class RegionalDockDoorClaimValidator(var rules: MutableList<RegionalDockDoorClaimRule>) {
    fun validate(item: RegionalDockDoorClaim): MutableList<String> {
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

    fun requireValid(item: RegionalDockDoorClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalDockDoorClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalDockDoorClaimValidator(): RegionalDockDoorClaimValidator {
    return RegionalDockDoorClaimValidator(
        mutableListOf<RegionalDockDoorClaimRule>(
            RegionalDockDoorClaimRequiredTimezoneRule("regional dock door claim needs this field"),
            RegionalDockDoorClaimRetriesRangeRule(64, 3600),
            RegionalDockDoorClaimRequiredNameRule("regional dock door claim needs an entry"),
            RegionalDockDoorClaimNotesLengthRule(1000),
            RegionalDockDoorClaimRequiredNotesRule("regional dock door claim needs an entry"),
        ),
    )
}
