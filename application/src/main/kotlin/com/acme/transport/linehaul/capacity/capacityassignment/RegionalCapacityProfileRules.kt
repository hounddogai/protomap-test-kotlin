package com.acme.transport.linehaul.capacity.capacityassignment

/** A rule a regional capacity profile must pass. Check returns its problems, or none. */
interface RegionalCapacityProfileRule {
    fun check(item: RegionalCapacityProfile): MutableList<String>
}

data class RegionalCapacityProfileCodePrefixRule(var prefix: String) : RegionalCapacityProfileRule {
    override fun check(item: RegionalCapacityProfile): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RegionalCapacityProfileRequiredNameRule(var message: String) : RegionalCapacityProfileRule {
    override fun check(item: RegionalCapacityProfile): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalCapacityProfileRequiredLabelRule(var message: String) : RegionalCapacityProfileRule {
    override fun check(item: RegionalCapacityProfile): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalCapacityProfileRequiredContactEmailRule(var message: String) : RegionalCapacityProfileRule {
    override fun check(item: RegionalCapacityProfile): MutableList<String> {
        if (item.contactEmail.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalCapacityProfileContactEmailLengthRule(var maxLength: Int) : RegionalCapacityProfileRule {
    override fun check(item: RegionalCapacityProfile): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalCapacityProfileNameLengthRule(var maxLength: Int) : RegionalCapacityProfileRule {
    override fun check(item: RegionalCapacityProfile): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional capacity profile. */
data class RegionalCapacityProfileValidator(var rules: MutableList<RegionalCapacityProfileRule>) {
    fun validate(item: RegionalCapacityProfile): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RegionalCapacityProfile) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalCapacityProfile>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalCapacityProfileValidator(): RegionalCapacityProfileValidator {
    return RegionalCapacityProfileValidator(
        mutableListOf<RegionalCapacityProfileRule>(
            RegionalCapacityProfileCodePrefixRule("ACM"),
            RegionalCapacityProfileRequiredNameRule("regional capacity profile needs an entry"),
            RegionalCapacityProfileRequiredLabelRule("regional capacity profile needs an entry"),
            RegionalCapacityProfileRequiredContactEmailRule("regional capacity profile needs this field"),
            RegionalCapacityProfileContactEmailLengthRule(255),
            RegionalCapacityProfileNameLengthRule(80),
        ),
    )
}
