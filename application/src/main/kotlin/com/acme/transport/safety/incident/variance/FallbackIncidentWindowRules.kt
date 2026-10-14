package com.acme.transport.safety.incident.variance

/** A rule a fallback incident window must pass. Check returns its problems, or none. */
interface FallbackIncidentWindowRule {
    fun check(item: FallbackIncidentWindow): MutableList<String>
}

data class FallbackIncidentWindowTitleLengthRule(var maxLength: Int) : FallbackIncidentWindowRule {
    override fun check(item: FallbackIncidentWindow): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackIncidentWindowRequiredLabelRule(var message: String) : FallbackIncidentWindowRule {
    override fun check(item: FallbackIncidentWindow): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackIncidentWindowTrailerIdLengthRule(var maxLength: Int) : FallbackIncidentWindowRule {
    override fun check(item: FallbackIncidentWindow): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback incident window. */
data class FallbackIncidentWindowValidator(var rules: MutableList<FallbackIncidentWindowRule>) {
    fun validate(item: FallbackIncidentWindow): MutableList<String> {
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

    fun requireValid(item: FallbackIncidentWindow) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackIncidentWindow>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackIncidentWindowValidator(): FallbackIncidentWindowValidator {
    return FallbackIncidentWindowValidator(
        mutableListOf<FallbackIncidentWindowRule>(
            FallbackIncidentWindowTitleLengthRule(120),
            FallbackIncidentWindowRequiredLabelRule("fallback incident window needs an entry"),
            FallbackIncidentWindowTrailerIdLengthRule(32),
        ),
    )
}
