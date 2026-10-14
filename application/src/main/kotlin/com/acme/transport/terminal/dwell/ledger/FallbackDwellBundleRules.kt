package com.acme.transport.terminal.dwell.ledger

/** A rule a fallback dwell bundle must pass. Check returns its problems, or none. */
interface FallbackDwellBundleRule {
    fun check(item: FallbackDwellBundle): MutableList<String>
}

data class FallbackDwellBundleRequiredCarrierRule(var message: String) : FallbackDwellBundleRule {
    override fun check(item: FallbackDwellBundle): MutableList<String> {
        if (item.carrier.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackDwellBundleGraceDaysRangeRule(var min: Int, var max: Int) : FallbackDwellBundleRule {
    override fun check(item: FallbackDwellBundle): MutableList<String> {
        val value = item.graceDays
        if (value < min || value > max) {
            return mutableListOf<String>("grace days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FallbackDwellBundleRequiredSegmentRule(var message: String) : FallbackDwellBundleRule {
    override fun check(item: FallbackDwellBundle): MutableList<String> {
        if (item.segment.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackDwellBundleRequiredCodeRule(var message: String) : FallbackDwellBundleRule {
    override fun check(item: FallbackDwellBundle): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackDwellBundleScoreRangeRule(var min: Double, var max: Double) : FallbackDwellBundleRule {
    override fun check(item: FallbackDwellBundle): MutableList<String> {
        val value = item.score
        if (value < min || value > max) {
            return mutableListOf<String>("score $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FallbackDwellBundleSegmentLengthRule(var maxLength: Int) : FallbackDwellBundleRule {
    override fun check(item: FallbackDwellBundle): MutableList<String> {
        val size = item.segment.length
        if (size > maxLength) {
            return mutableListOf<String>("segment is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback dwell bundle. */
data class FallbackDwellBundleValidator(var rules: MutableList<FallbackDwellBundleRule>) {
    fun validate(item: FallbackDwellBundle): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FallbackDwellBundle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackDwellBundle>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackDwellBundleValidator(): FallbackDwellBundleValidator {
    return FallbackDwellBundleValidator(
        mutableListOf<FallbackDwellBundleRule>(
            FallbackDwellBundleRequiredCarrierRule("fallback dwell bundle needs this field"),
            FallbackDwellBundleGraceDaysRangeRule(120, 3600),
            FallbackDwellBundleRequiredSegmentRule("fallback dwell bundle needs an entry"),
            FallbackDwellBundleRequiredCodeRule("fallback dwell bundle needs this field"),
            FallbackDwellBundleScoreRangeRule(6.0, 2771.3),
            FallbackDwellBundleSegmentLengthRule(86400),
        ),
    )
}
