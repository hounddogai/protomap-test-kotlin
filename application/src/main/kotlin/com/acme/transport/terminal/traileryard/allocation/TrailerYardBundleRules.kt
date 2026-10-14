package com.acme.transport.terminal.traileryard.allocation

/** A rule a trailer yard bundle must pass. Check returns its problems, or none. */
interface TrailerYardBundleRule {
    fun check(item: TrailerYardBundle): MutableList<String>
}

data class TrailerYardBundleRequiredTrailerIdRule(var message: String) : TrailerYardBundleRule {
    override fun check(item: TrailerYardBundle): MutableList<String> {
        if (item.trailerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TrailerYardBundleRetriesRangeRule(var min: Int, var max: Int) : TrailerYardBundleRule {
    override fun check(item: TrailerYardBundle): MutableList<String> {
        val value = item.retries
        if (value < min || value > max) {
            return mutableListOf<String>("retries $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TrailerYardBundleRequiredShiftCodeRule(var message: String) : TrailerYardBundleRule {
    override fun check(item: TrailerYardBundle): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TrailerYardBundleRequiredNameRule(var message: String) : TrailerYardBundleRule {
    override fun check(item: TrailerYardBundle): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a trailer yard bundle. */
data class TrailerYardBundleValidator(var rules: MutableList<TrailerYardBundleRule>) {
    fun validate(item: TrailerYardBundle): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TrailerYardBundle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TrailerYardBundle>): MutableMap<String, MutableList<String>> {
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

fun defaultTrailerYardBundleValidator(): TrailerYardBundleValidator {
    return TrailerYardBundleValidator(
        mutableListOf<TrailerYardBundleRule>(
            TrailerYardBundleRequiredTrailerIdRule("trailer yard bundle needs an entry"),
            TrailerYardBundleRetriesRangeRule(64, 1000),
            TrailerYardBundleRequiredShiftCodeRule("trailer yard bundle needs a value"),
            TrailerYardBundleRequiredNameRule("trailer yard bundle needs an entry"),
        ),
    )
}
