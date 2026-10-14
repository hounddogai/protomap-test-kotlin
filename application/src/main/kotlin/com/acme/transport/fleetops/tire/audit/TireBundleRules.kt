package com.acme.transport.fleetops.tire.audit

/** A rule a tire bundle must pass. Check returns its problems, or none. */
interface TireBundleRule {
    fun check(item: TireBundle): MutableList<String>
}

data class TireBundleNameLengthRule(var maxLength: Int) : TireBundleRule {
    override fun check(item: TireBundle): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TireBundleRequiredSourceRule(var message: String) : TireBundleRule {
    override fun check(item: TireBundle): MutableList<String> {
        if (item.source.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TireBundleCodePrefixRule(var prefix: String) : TireBundleRule {
    override fun check(item: TireBundle): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TireBundleRequiredCodeRule(var message: String) : TireBundleRule {
    override fun check(item: TireBundle): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tire bundle. */
data class TireBundleValidator(var rules: MutableList<TireBundleRule>) {
    fun validate(item: TireBundle): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TireBundle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TireBundle>): MutableMap<String, MutableList<String>> {
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

fun defaultTireBundleValidator(): TireBundleValidator {
    return TireBundleValidator(
        mutableListOf<TireBundleRule>(
            TireBundleNameLengthRule(3600),
            TireBundleRequiredSourceRule("tire bundle needs this field"),
            TireBundleCodePrefixRule("Q"),
            TireBundleRequiredCodeRule("tire bundle needs this field"),
        ),
    )
}
