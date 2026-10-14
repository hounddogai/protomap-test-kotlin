package com.acme.transport.safety.incident.request

/** A rule a seasonal incident claim must pass. Check returns its problems, or none. */
interface SeasonalIncidentClaimRule {
    fun check(item: SeasonalIncidentClaim): MutableList<String>
}

data class SeasonalIncidentClaimTitleLengthRule(var maxLength: Int) : SeasonalIncidentClaimRule {
    override fun check(item: SeasonalIncidentClaim): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalIncidentClaimRequiredTitleRule(var message: String) : SeasonalIncidentClaimRule {
    override fun check(item: SeasonalIncidentClaim): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalIncidentClaimCodeLengthRule(var maxLength: Int) : SeasonalIncidentClaimRule {
    override fun check(item: SeasonalIncidentClaim): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalIncidentClaimLabelLengthRule(var maxLength: Int) : SeasonalIncidentClaimRule {
    override fun check(item: SeasonalIncidentClaim): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalIncidentClaimRequiredLabelRule(var message: String) : SeasonalIncidentClaimRule {
    override fun check(item: SeasonalIncidentClaim): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal incident claim. */
data class SeasonalIncidentClaimValidator(var rules: MutableList<SeasonalIncidentClaimRule>) {
    fun validate(item: SeasonalIncidentClaim): MutableList<String> {
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

    fun requireValid(item: SeasonalIncidentClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalIncidentClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalIncidentClaimValidator(): SeasonalIncidentClaimValidator {
    return SeasonalIncidentClaimValidator(
        mutableListOf<SeasonalIncidentClaimRule>(
            SeasonalIncidentClaimTitleLengthRule(16),
            SeasonalIncidentClaimRequiredTitleRule("seasonal incident claim needs an entry"),
            SeasonalIncidentClaimCodeLengthRule(80),
            SeasonalIncidentClaimLabelLengthRule(3600),
            SeasonalIncidentClaimRequiredLabelRule("seasonal incident claim needs an entry"),
        ),
    )
}
