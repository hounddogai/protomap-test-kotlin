package com.acme.transport.terminal.traileryard.manifest

/** A rule a trailer yard manifest must pass. Check returns its problems, or none. */
interface TrailerYardManifestRule {
    fun check(item: TrailerYardManifest): MutableList<String>
}

data class TrailerYardManifestPriorityRangeRule(var min: Int, var max: Int) : TrailerYardManifestRule {
    override fun check(item: TrailerYardManifest): MutableList<String> {
        val value = item.priority
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("priority $value is below $min")
        }
        if (value > max) {
            problems.add("priority $value is above $max")
        }
        return problems
    }
}

data class TrailerYardManifestCostRangeRule(var min: Double, var max: Double) : TrailerYardManifestRule {
    override fun check(item: TrailerYardManifest): MutableList<String> {
        val value = item.cost
        if (value < min || value > max) {
            return mutableListOf<String>("cost $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TrailerYardManifestRequiredCodeRule(var message: String) : TrailerYardManifestRule {
    override fun check(item: TrailerYardManifest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TrailerYardManifestGraceDaysRangeRule(var min: Int, var max: Int) : TrailerYardManifestRule {
    override fun check(item: TrailerYardManifest): MutableList<String> {
        val value = item.graceDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("grace days $value is below $min")
        }
        if (value > max) {
            problems.add("grace days $value is above $max")
        }
        return problems
    }
}

data class TrailerYardManifestRequiredTitleRule(var message: String) : TrailerYardManifestRule {
    override fun check(item: TrailerYardManifest): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TrailerYardManifestCodePrefixRule(var prefix: String) : TrailerYardManifestRule {
    override fun check(item: TrailerYardManifest): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a trailer yard manifest. */
data class TrailerYardManifestValidator(var rules: MutableList<TrailerYardManifestRule>) {
    fun validate(item: TrailerYardManifest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TrailerYardManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TrailerYardManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultTrailerYardManifestValidator(): TrailerYardManifestValidator {
    return TrailerYardManifestValidator(
        mutableListOf<TrailerYardManifestRule>(
            TrailerYardManifestPriorityRangeRule(32, 120),
            TrailerYardManifestCostRangeRule(1.8, 1109.6),
            TrailerYardManifestRequiredCodeRule("trailer yard manifest needs an entry"),
            TrailerYardManifestGraceDaysRangeRule(80, 1000),
            TrailerYardManifestRequiredTitleRule("trailer yard manifest needs a value"),
            TrailerYardManifestCodePrefixRule("REF"),
        ),
    )
}
