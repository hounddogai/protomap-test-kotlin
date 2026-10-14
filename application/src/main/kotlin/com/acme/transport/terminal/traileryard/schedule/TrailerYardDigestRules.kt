package com.acme.transport.terminal.traileryard.schedule

/** A rule a trailer yard digest must pass. Check returns its problems, or none. */
interface TrailerYardDigestRule {
    fun check(item: TrailerYardDigest): MutableList<String>
}

data class TrailerYardDigestCodePrefixRule(var prefix: String) : TrailerYardDigestRule {
    override fun check(item: TrailerYardDigest): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TrailerYardDigestRequiredLabelRule(var message: String) : TrailerYardDigestRule {
    override fun check(item: TrailerYardDigest): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TrailerYardDigestLabelLengthRule(var maxLength: Int) : TrailerYardDigestRule {
    override fun check(item: TrailerYardDigest): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a trailer yard digest. */
data class TrailerYardDigestValidator(var rules: MutableList<TrailerYardDigestRule>) {
    fun validate(item: TrailerYardDigest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TrailerYardDigest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TrailerYardDigest>): MutableMap<String, MutableList<String>> {
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

fun defaultTrailerYardDigestValidator(): TrailerYardDigestValidator {
    return TrailerYardDigestValidator(
        mutableListOf<TrailerYardDigestRule>(
            TrailerYardDigestCodePrefixRule("REF"),
            TrailerYardDigestRequiredLabelRule("trailer yard digest needs a value"),
            TrailerYardDigestLabelLengthRule(64),
        ),
    )
}
