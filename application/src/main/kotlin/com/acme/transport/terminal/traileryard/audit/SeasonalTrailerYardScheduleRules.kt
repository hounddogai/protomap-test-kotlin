package com.acme.transport.terminal.traileryard.audit

/** A rule a seasonal trailer yard schedule must pass. Check returns its problems, or none. */
interface SeasonalTrailerYardScheduleRule {
    fun check(item: SeasonalTrailerYardSchedule): MutableList<String>
}

data class SeasonalTrailerYardScheduleCodeLengthRule(var maxLength: Int) : SeasonalTrailerYardScheduleRule {
    override fun check(item: SeasonalTrailerYardSchedule): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalTrailerYardScheduleNameLengthRule(var maxLength: Int) : SeasonalTrailerYardScheduleRule {
    override fun check(item: SeasonalTrailerYardSchedule): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalTrailerYardScheduleRequiredNameRule(var message: String) : SeasonalTrailerYardScheduleRule {
    override fun check(item: SeasonalTrailerYardSchedule): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalTrailerYardScheduleRequiredCategoryRule(
    var message: String,
) : SeasonalTrailerYardScheduleRule {
    override fun check(item: SeasonalTrailerYardSchedule): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalTrailerYardScheduleCodePrefixRule(var prefix: String) : SeasonalTrailerYardScheduleRule {
    override fun check(item: SeasonalTrailerYardSchedule): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalTrailerYardScheduleRequiredCodeRule(var message: String) : SeasonalTrailerYardScheduleRule {
    override fun check(item: SeasonalTrailerYardSchedule): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal trailer yard schedule. */
data class SeasonalTrailerYardScheduleValidator(var rules: MutableList<SeasonalTrailerYardScheduleRule>) {
    fun validate(item: SeasonalTrailerYardSchedule): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SeasonalTrailerYardSchedule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalTrailerYardSchedule>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalTrailerYardScheduleValidator(): SeasonalTrailerYardScheduleValidator {
    return SeasonalTrailerYardScheduleValidator(
        mutableListOf<SeasonalTrailerYardScheduleRule>(
            SeasonalTrailerYardScheduleCodeLengthRule(255),
            SeasonalTrailerYardScheduleNameLengthRule(80),
            SeasonalTrailerYardScheduleRequiredNameRule("seasonal trailer yard schedule needs an entry"),
            SeasonalTrailerYardScheduleRequiredCategoryRule("seasonal trailer yard schedule needs an entry"),
            SeasonalTrailerYardScheduleCodePrefixRule("REF"),
            SeasonalTrailerYardScheduleRequiredCodeRule("seasonal trailer yard schedule needs an entry"),
        ),
    )
}
