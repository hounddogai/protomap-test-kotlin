package com.acme.transport.linehaul.capacity.slot

/** A rule a regional capacity request must pass. Check returns its problems, or none. */
interface RegionalCapacityRequestRule {
    fun check(item: RegionalCapacityRequest): MutableList<String>
}

data class RegionalCapacityRequestCodePrefixRule(var prefix: String) : RegionalCapacityRequestRule {
    override fun check(item: RegionalCapacityRequest): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RegionalCapacityRequestVersionRangeRule(var min: Int, var max: Int) : RegionalCapacityRequestRule {
    override fun check(item: RegionalCapacityRequest): MutableList<String> {
        val value = item.version
        if (value < min || value > max) {
            return mutableListOf<String>("version $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RegionalCapacityRequestChecksumLengthRule(var maxLength: Int) : RegionalCapacityRequestRule {
    override fun check(item: RegionalCapacityRequest): MutableList<String> {
        val size = item.checksum.length
        if (size > maxLength) {
            return mutableListOf<String>("checksum is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalCapacityRequestRequiredNameRule(var message: String) : RegionalCapacityRequestRule {
    override fun check(item: RegionalCapacityRequest): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalCapacityRequestRequiredChecksumRule(var message: String) : RegionalCapacityRequestRule {
    override fun check(item: RegionalCapacityRequest): MutableList<String> {
        if (item.checksum.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional capacity request. */
data class RegionalCapacityRequestValidator(var rules: MutableList<RegionalCapacityRequestRule>) {
    fun validate(item: RegionalCapacityRequest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RegionalCapacityRequest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalCapacityRequest>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalCapacityRequestValidator(): RegionalCapacityRequestValidator {
    return RegionalCapacityRequestValidator(
        mutableListOf<RegionalCapacityRequestRule>(
            RegionalCapacityRequestCodePrefixRule("ACM"),
            RegionalCapacityRequestVersionRangeRule(32, 64),
            RegionalCapacityRequestChecksumLengthRule(3600),
            RegionalCapacityRequestRequiredNameRule("regional capacity request needs this field"),
            RegionalCapacityRequestRequiredChecksumRule("regional capacity request needs a value"),
        ),
    )
}
