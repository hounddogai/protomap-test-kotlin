package com.acme.transport.terminal.traileryard.policy

/** A rule a rolling trailer yard bundle must pass. Check returns its problems, or none. */
interface RollingTrailerYardBundleRule {
    fun check(item: RollingTrailerYardBundle): MutableList<String>
}

data class RollingTrailerYardBundleLabelLengthRule(var maxLength: Int) : RollingTrailerYardBundleRule {
    override fun check(item: RollingTrailerYardBundle): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingTrailerYardBundleStopCountRangeRule(
    var min: Int,
    var max: Int,
) : RollingTrailerYardBundleRule {
    override fun check(item: RollingTrailerYardBundle): MutableList<String> {
        val value = item.stopCount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("stop count $value is below $min")
        }
        if (value > max) {
            problems.add("stop count $value is above $max")
        }
        return problems
    }
}

data class RollingTrailerYardBundleCodePrefixRule(var prefix: String) : RollingTrailerYardBundleRule {
    override fun check(item: RollingTrailerYardBundle): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RollingTrailerYardBundleShiftCodeLengthRule(var maxLength: Int) : RollingTrailerYardBundleRule {
    override fun check(item: RollingTrailerYardBundle): MutableList<String> {
        val size = item.shiftCode.length
        if (size > maxLength) {
            return mutableListOf<String>("shift code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingTrailerYardBundleCodeLengthRule(var maxLength: Int) : RollingTrailerYardBundleRule {
    override fun check(item: RollingTrailerYardBundle): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling trailer yard bundle. */
data class RollingTrailerYardBundleValidator(var rules: MutableList<RollingTrailerYardBundleRule>) {
    fun validate(item: RollingTrailerYardBundle): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RollingTrailerYardBundle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingTrailerYardBundle>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingTrailerYardBundleValidator(): RollingTrailerYardBundleValidator {
    return RollingTrailerYardBundleValidator(
        mutableListOf<RollingTrailerYardBundleRule>(
            RollingTrailerYardBundleLabelLengthRule(86400),
            RollingTrailerYardBundleStopCountRangeRule(80, 120),
            RollingTrailerYardBundleCodePrefixRule("X"),
            RollingTrailerYardBundleShiftCodeLengthRule(80),
            RollingTrailerYardBundleCodeLengthRule(16),
        ),
    )
}
