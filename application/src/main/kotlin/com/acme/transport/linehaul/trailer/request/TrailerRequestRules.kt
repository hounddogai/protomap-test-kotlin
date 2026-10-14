package com.acme.transport.linehaul.trailer.request

/** A rule a trailer request must pass. Check returns its problems, or none. */
interface TrailerRequestRule {
    fun check(item: TrailerRequest): MutableList<String>
}

data class TrailerRequestCountryLengthRule(var maxLength: Int) : TrailerRequestRule {
    override fun check(item: TrailerRequest): MutableList<String> {
        val size = item.country.length
        if (size > maxLength) {
            return mutableListOf<String>("country is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TrailerRequestRequiredNotesRule(var message: String) : TrailerRequestRule {
    override fun check(item: TrailerRequest): MutableList<String> {
        if (item.notes.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TrailerRequestCodePrefixRule(var prefix: String) : TrailerRequestRule {
    override fun check(item: TrailerRequest): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a trailer request. */
data class TrailerRequestValidator(var rules: MutableList<TrailerRequestRule>) {
    fun validate(item: TrailerRequest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TrailerRequest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TrailerRequest>): MutableMap<String, MutableList<String>> {
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

fun defaultTrailerRequestValidator(): TrailerRequestValidator {
    return TrailerRequestValidator(
        mutableListOf<TrailerRequestRule>(
            TrailerRequestCountryLengthRule(120),
            TrailerRequestRequiredNotesRule("trailer request needs this field"),
            TrailerRequestCodePrefixRule("REF"),
        ),
    )
}
