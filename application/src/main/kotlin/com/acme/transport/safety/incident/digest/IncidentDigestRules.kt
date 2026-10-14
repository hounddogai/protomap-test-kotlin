package com.acme.transport.safety.incident.digest

/** A rule a incident digest must pass. Check returns its problems, or none. */
interface IncidentDigestRule {
    fun check(item: IncidentDigest): MutableList<String>
}

data class IncidentDigestRequiredCodeRule(var message: String) : IncidentDigestRule {
    override fun check(item: IncidentDigest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class IncidentDigestArchivedGuardRule(var limit: Double) : IncidentDigestRule {
    override fun check(item: IncidentDigest): MutableList<String> {
        if (item.archived && item.loadKg > limit) {
            return mutableListOf<String>("load kg ${item.loadKg} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

data class IncidentDigestRequiredTimezoneRule(var message: String) : IncidentDigestRule {
    override fun check(item: IncidentDigest): MutableList<String> {
        if (item.timezone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class IncidentDigestRequiredTitleRule(var message: String) : IncidentDigestRule {
    override fun check(item: IncidentDigest): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class IncidentDigestTitleLengthRule(var maxLength: Int) : IncidentDigestRule {
    override fun check(item: IncidentDigest): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class IncidentDigestLoadKgRangeRule(var min: Double, var max: Double) : IncidentDigestRule {
    override fun check(item: IncidentDigest): MutableList<String> {
        val value = item.loadKg
        if (value < min || value > max) {
            return mutableListOf<String>("load kg $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a incident digest. */
data class IncidentDigestValidator(var rules: MutableList<IncidentDigestRule>) {
    fun validate(item: IncidentDigest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: IncidentDigest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<IncidentDigest>): MutableMap<String, MutableList<String>> {
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

fun defaultIncidentDigestValidator(): IncidentDigestValidator {
    return IncidentDigestValidator(
        mutableListOf<IncidentDigestRule>(
            IncidentDigestRequiredCodeRule("incident digest needs an entry"),
            IncidentDigestArchivedGuardRule(5.2),
            IncidentDigestRequiredTimezoneRule("incident digest needs an entry"),
            IncidentDigestRequiredTitleRule("incident digest needs an entry"),
            IncidentDigestTitleLengthRule(120),
            IncidentDigestLoadKgRangeRule(7.7, 4341.2),
        ),
    )
}
