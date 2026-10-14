package com.acme.transport.customs.declaration.declarationmanifest

/** A rule a rolling declaration group must pass. Check returns its problems, or none. */
interface RollingDeclarationGroupRule {
    fun check(item: RollingDeclarationGroup): MutableList<String>
}

data class RollingDeclarationGroupWindowDaysRangeRule(
    var min: Int,
    var max: Int,
) : RollingDeclarationGroupRule {
    override fun check(item: RollingDeclarationGroup): MutableList<String> {
        val value = item.windowDays
        if (value < min || value > max) {
            return mutableListOf<String>("window days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RollingDeclarationGroupRequiredCategoryRule(var message: String) : RollingDeclarationGroupRule {
    override fun check(item: RollingDeclarationGroup): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingDeclarationGroupMaximumRangeRule(
    var min: Double,
    var max: Double,
) : RollingDeclarationGroupRule {
    override fun check(item: RollingDeclarationGroup): MutableList<String> {
        val value = item.maximum
        if (value < min || value > max) {
            return mutableListOf<String>("maximum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RollingDeclarationGroupTitleLengthRule(var maxLength: Int) : RollingDeclarationGroupRule {
    override fun check(item: RollingDeclarationGroup): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling declaration group. */
data class RollingDeclarationGroupValidator(var rules: MutableList<RollingDeclarationGroupRule>) {
    fun validate(item: RollingDeclarationGroup): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RollingDeclarationGroup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingDeclarationGroup>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingDeclarationGroupValidator(): RollingDeclarationGroupValidator {
    return RollingDeclarationGroupValidator(
        mutableListOf<RollingDeclarationGroupRule>(
            RollingDeclarationGroupWindowDaysRangeRule(80, 255),
            RollingDeclarationGroupRequiredCategoryRule("rolling declaration group needs a value"),
            RollingDeclarationGroupMaximumRangeRule(4.3, 4460.1),
            RollingDeclarationGroupTitleLengthRule(86400),
        ),
    )
}
