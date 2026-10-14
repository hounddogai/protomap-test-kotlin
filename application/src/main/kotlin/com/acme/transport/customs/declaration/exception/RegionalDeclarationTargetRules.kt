package com.acme.transport.customs.declaration.exception

/** A rule a regional declaration target must pass. Check returns its problems, or none. */
interface RegionalDeclarationTargetRule {
    fun check(item: RegionalDeclarationTarget): MutableList<String>
}

data class RegionalDeclarationTargetRequiredTimezoneRule(var message: String) : RegionalDeclarationTargetRule {
    override fun check(item: RegionalDeclarationTarget): MutableList<String> {
        if (item.timezone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalDeclarationTargetRatioRangeRule(
    var min: Double,
    var max: Double,
) : RegionalDeclarationTargetRule {
    override fun check(item: RegionalDeclarationTarget): MutableList<String> {
        val value = item.ratio
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ratio $value is below $min")
        }
        if (value > max) {
            problems.add("ratio $value is above $max")
        }
        return problems
    }
}

data class RegionalDeclarationTargetCeilingRangeRule(
    var min: Double,
    var max: Double,
) : RegionalDeclarationTargetRule {
    override fun check(item: RegionalDeclarationTarget): MutableList<String> {
        val value = item.ceiling
        if (value < min || value > max) {
            return mutableListOf<String>("ceiling $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RegionalDeclarationTargetTimezoneLengthRule(var maxLength: Int) : RegionalDeclarationTargetRule {
    override fun check(item: RegionalDeclarationTarget): MutableList<String> {
        val size = item.timezone.length
        if (size > maxLength) {
            return mutableListOf<String>("timezone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalDeclarationTargetNameLengthRule(var maxLength: Int) : RegionalDeclarationTargetRule {
    override fun check(item: RegionalDeclarationTarget): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional declaration target. */
data class RegionalDeclarationTargetValidator(var rules: MutableList<RegionalDeclarationTargetRule>) {
    fun validate(item: RegionalDeclarationTarget): MutableList<String> {
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

    fun requireValid(item: RegionalDeclarationTarget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalDeclarationTarget>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalDeclarationTargetValidator(): RegionalDeclarationTargetValidator {
    return RegionalDeclarationTargetValidator(
        mutableListOf<RegionalDeclarationTargetRule>(
            RegionalDeclarationTargetRequiredTimezoneRule("regional declaration target needs an entry"),
            RegionalDeclarationTargetRatioRangeRule(5.6, 1527.7),
            RegionalDeclarationTargetCeilingRangeRule(3.0, 4590.7),
            RegionalDeclarationTargetTimezoneLengthRule(86400),
            RegionalDeclarationTargetNameLengthRule(3600),
        ),
    )
}
