package com.acme.transport.emissions.fuelburn.group

/** A rule a fuel burn window must pass. Check returns its problems, or none. */
interface FuelBurnWindowRule {
    fun check(item: FuelBurnWindow): MutableList<String>
}

data class FuelBurnWindowRequiredDescriptionRule(var message: String) : FuelBurnWindowRule {
    override fun check(item: FuelBurnWindow): MutableList<String> {
        if (item.description.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FuelBurnWindowVisibleGuardRule(var limit: Double) : FuelBurnWindowRule {
    override fun check(item: FuelBurnWindow): MutableList<String> {
        if (item.visible && item.margin > limit) {
            return mutableListOf<String>("margin ${item.margin} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

data class FuelBurnWindowRequiredTitleRule(var message: String) : FuelBurnWindowRule {
    override fun check(item: FuelBurnWindow): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FuelBurnWindowCodePrefixRule(var prefix: String) : FuelBurnWindowRule {
    override fun check(item: FuelBurnWindow): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class FuelBurnWindowDescriptionLengthRule(var maxLength: Int) : FuelBurnWindowRule {
    override fun check(item: FuelBurnWindow): MutableList<String> {
        val size = item.description.length
        if (size > maxLength) {
            return mutableListOf<String>("description is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fuel burn window. */
data class FuelBurnWindowValidator(var rules: MutableList<FuelBurnWindowRule>) {
    fun validate(item: FuelBurnWindow): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FuelBurnWindow) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FuelBurnWindow>): MutableMap<String, MutableList<String>> {
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

fun defaultFuelBurnWindowValidator(): FuelBurnWindowValidator {
    return FuelBurnWindowValidator(
        mutableListOf<FuelBurnWindowRule>(
            FuelBurnWindowRequiredDescriptionRule("fuel burn window needs this field"),
            FuelBurnWindowVisibleGuardRule(9.7),
            FuelBurnWindowRequiredTitleRule("fuel burn window needs an entry"),
            FuelBurnWindowCodePrefixRule("ACM"),
            FuelBurnWindowDescriptionLengthRule(80),
        ),
    )
}
