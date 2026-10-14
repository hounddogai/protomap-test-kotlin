package com.acme.transport.crewing.qualification.request

/** A rule a regional qualification window must pass. Check returns its problems, or none. */
interface RegionalQualificationWindowRule {
    fun check(item: RegionalQualificationWindow): MutableList<String>
}

data class RegionalQualificationWindowRequiredNameRule(var message: String) : RegionalQualificationWindowRule {
    override fun check(item: RegionalQualificationWindow): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalQualificationWindowNameLengthRule(var maxLength: Int) : RegionalQualificationWindowRule {
    override fun check(item: RegionalQualificationWindow): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalQualificationWindowEtaMinutesRangeRule(
    var min: Int,
    var max: Int,
) : RegionalQualificationWindowRule {
    override fun check(item: RegionalQualificationWindow): MutableList<String> {
        val value = item.etaMinutes
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("eta minutes $value is below $min")
        }
        if (value > max) {
            problems.add("eta minutes $value is above $max")
        }
        return problems
    }
}

data class RegionalQualificationWindowCodeLengthRule(var maxLength: Int) : RegionalQualificationWindowRule {
    override fun check(item: RegionalQualificationWindow): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional qualification window. */
data class RegionalQualificationWindowValidator(var rules: MutableList<RegionalQualificationWindowRule>) {
    fun validate(item: RegionalQualificationWindow): MutableList<String> {
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

    fun requireValid(item: RegionalQualificationWindow) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalQualificationWindow>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalQualificationWindowValidator(): RegionalQualificationWindowValidator {
    return RegionalQualificationWindowValidator(
        mutableListOf<RegionalQualificationWindowRule>(
            RegionalQualificationWindowRequiredNameRule("regional qualification window needs an entry"),
            RegionalQualificationWindowNameLengthRule(120),
            RegionalQualificationWindowEtaMinutesRangeRule(32, 3600),
            RegionalQualificationWindowCodeLengthRule(64),
        ),
    )
}
