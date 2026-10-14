package com.acme.transport.emissions.routeoffset.digest

/** A rule a default route offset agreement must pass. Check returns its problems, or none. */
interface DefaultRouteOffsetAgreementRule {
    fun check(item: DefaultRouteOffsetAgreement): MutableList<String>
}

data class DefaultRouteOffsetAgreementRequiredTitleRule(var message: String) : DefaultRouteOffsetAgreementRule {
    override fun check(item: DefaultRouteOffsetAgreement): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultRouteOffsetAgreementStartsAtRangeRule(
    var min: Int,
    var max: Int,
) : DefaultRouteOffsetAgreementRule {
    override fun check(item: DefaultRouteOffsetAgreement): MutableList<String> {
        val value = item.startsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("starts at $value is below $min")
        }
        if (value > max) {
            problems.add("starts at $value is above $max")
        }
        return problems
    }
}

data class DefaultRouteOffsetAgreementNotesLengthRule(var maxLength: Int) : DefaultRouteOffsetAgreementRule {
    override fun check(item: DefaultRouteOffsetAgreement): MutableList<String> {
        val size = item.notes.length
        if (size > maxLength) {
            return mutableListOf<String>("notes is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultRouteOffsetAgreementCodePrefixRule(var prefix: String) : DefaultRouteOffsetAgreementRule {
    override fun check(item: DefaultRouteOffsetAgreement): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DefaultRouteOffsetAgreementRequiredDriverIdRule(
    var message: String,
) : DefaultRouteOffsetAgreementRule {
    override fun check(item: DefaultRouteOffsetAgreement): MutableList<String> {
        if (item.driverId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultRouteOffsetAgreementTitleLengthRule(var maxLength: Int) : DefaultRouteOffsetAgreementRule {
    override fun check(item: DefaultRouteOffsetAgreement): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default route offset agreement. */
data class DefaultRouteOffsetAgreementValidator(var rules: MutableList<DefaultRouteOffsetAgreementRule>) {
    fun validate(item: DefaultRouteOffsetAgreement): MutableList<String> {
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

    fun requireValid(item: DefaultRouteOffsetAgreement) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultRouteOffsetAgreement>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultRouteOffsetAgreementValidator(): DefaultRouteOffsetAgreementValidator {
    return DefaultRouteOffsetAgreementValidator(
        mutableListOf<DefaultRouteOffsetAgreementRule>(
            DefaultRouteOffsetAgreementRequiredTitleRule("default route offset agreement needs an entry"),
            DefaultRouteOffsetAgreementStartsAtRangeRule(80, 3600),
            DefaultRouteOffsetAgreementNotesLengthRule(3600),
            DefaultRouteOffsetAgreementCodePrefixRule("OPS"),
            DefaultRouteOffsetAgreementRequiredDriverIdRule("default route offset agreement needs an entry"),
            DefaultRouteOffsetAgreementTitleLengthRule(3600),
        ),
    )
}
