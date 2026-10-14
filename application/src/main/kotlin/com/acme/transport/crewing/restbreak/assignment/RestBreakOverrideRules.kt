package com.acme.transport.crewing.restbreak.assignment

/** A rule a rest break override must pass. Check returns its problems, or none. */
interface RestBreakOverrideRule {
    fun check(item: RestBreakOverride): MutableList<String>
}

data class RestBreakOverrideNameLengthRule(var maxLength: Int) : RestBreakOverrideRule {
    override fun check(item: RestBreakOverride): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RestBreakOverrideRegionLengthRule(var maxLength: Int) : RestBreakOverrideRule {
    override fun check(item: RestBreakOverride): MutableList<String> {
        val size = item.region.length
        if (size > maxLength) {
            return mutableListOf<String>("region is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RestBreakOverrideRequiredNameRule(var message: String) : RestBreakOverrideRule {
    override fun check(item: RestBreakOverride): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RestBreakOverrideRequiredCarrierRule(var message: String) : RestBreakOverrideRule {
    override fun check(item: RestBreakOverride): MutableList<String> {
        if (item.carrier.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RestBreakOverrideRequiredCodeRule(var message: String) : RestBreakOverrideRule {
    override fun check(item: RestBreakOverride): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rest break override. */
data class RestBreakOverrideValidator(var rules: MutableList<RestBreakOverrideRule>) {
    fun validate(item: RestBreakOverride): MutableList<String> {
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

    fun requireValid(item: RestBreakOverride) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RestBreakOverride>): MutableMap<String, MutableList<String>> {
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

fun defaultRestBreakOverrideValidator(): RestBreakOverrideValidator {
    return RestBreakOverrideValidator(
        mutableListOf<RestBreakOverrideRule>(
            RestBreakOverrideNameLengthRule(120),
            RestBreakOverrideRegionLengthRule(16),
            RestBreakOverrideRequiredNameRule("rest break override needs an entry"),
            RestBreakOverrideRequiredCarrierRule("rest break override needs a value"),
            RestBreakOverrideRequiredCodeRule("rest break override needs a value"),
        ),
    )
}
