package com.acme.transport.network.node.bundle

/** A rule a strict node bundle must pass. Check returns its problems, or none. */
interface StrictNodeBundleRule {
    fun check(item: StrictNodeBundle): MutableList<String>
}

data class StrictNodeBundleRequiredReviewerRule(var message: String) : StrictNodeBundleRule {
    override fun check(item: StrictNodeBundle): MutableList<String> {
        if (item.reviewer.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictNodeBundleRequiredSegmentRule(var message: String) : StrictNodeBundleRule {
    override fun check(item: StrictNodeBundle): MutableList<String> {
        if (item.segment.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictNodeBundleRequiredLabelRule(var message: String) : StrictNodeBundleRule {
    override fun check(item: StrictNodeBundle): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictNodeBundleCodePrefixRule(var prefix: String) : StrictNodeBundleRule {
    override fun check(item: StrictNodeBundle): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict node bundle. */
data class StrictNodeBundleValidator(var rules: MutableList<StrictNodeBundleRule>) {
    fun validate(item: StrictNodeBundle): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: StrictNodeBundle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictNodeBundle>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictNodeBundleValidator(): StrictNodeBundleValidator {
    return StrictNodeBundleValidator(
        mutableListOf<StrictNodeBundleRule>(
            StrictNodeBundleRequiredReviewerRule("strict node bundle needs a value"),
            StrictNodeBundleRequiredSegmentRule("strict node bundle needs this field"),
            StrictNodeBundleRequiredLabelRule("strict node bundle needs an entry"),
            StrictNodeBundleCodePrefixRule("ACM"),
        ),
    )
}
