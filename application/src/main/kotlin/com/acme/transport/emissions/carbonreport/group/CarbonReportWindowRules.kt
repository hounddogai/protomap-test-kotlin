package com.acme.transport.emissions.carbonreport.group

/** A rule a carbon report window must pass. Check returns its problems, or none. */
interface CarbonReportWindowRule {
    fun check(item: CarbonReportWindow): MutableList<String>
}

data class CarbonReportWindowPinnedGuardRule(var limit: Int) : CarbonReportWindowRule {
    override fun check(item: CarbonReportWindow): MutableList<String> {
        if (item.pinned && item.durationMinutes > limit) {
            return mutableListOf<String>("duration minutes ${item.durationMinutes} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

data class CarbonReportWindowChannelLengthRule(var maxLength: Int) : CarbonReportWindowRule {
    override fun check(item: CarbonReportWindow): MutableList<String> {
        val size = item.channel.length
        if (size > maxLength) {
            return mutableListOf<String>("channel is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CarbonReportWindowCodeLengthRule(var maxLength: Int) : CarbonReportWindowRule {
    override fun check(item: CarbonReportWindow): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CarbonReportWindowRatioRangeRule(var min: Double, var max: Double) : CarbonReportWindowRule {
    override fun check(item: CarbonReportWindow): MutableList<String> {
        val value = item.ratio
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ratio $value is below $min")
        }
        if (value > max) {
            problems.add("ratio $value is above $max")
        }
        return problems
    }
}

data class CarbonReportWindowLabelLengthRule(var maxLength: Int) : CarbonReportWindowRule {
    override fun check(item: CarbonReportWindow): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CarbonReportWindowCodePrefixRule(var prefix: String) : CarbonReportWindowRule {
    override fun check(item: CarbonReportWindow): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a carbon report window. */
data class CarbonReportWindowValidator(var rules: MutableList<CarbonReportWindowRule>) {
    fun validate(item: CarbonReportWindow): MutableList<String> {
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

    fun requireValid(item: CarbonReportWindow) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CarbonReportWindow>): MutableMap<String, MutableList<String>> {
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

fun defaultCarbonReportWindowValidator(): CarbonReportWindowValidator {
    return CarbonReportWindowValidator(
        mutableListOf<CarbonReportWindowRule>(
            CarbonReportWindowPinnedGuardRule(1000),
            CarbonReportWindowChannelLengthRule(64),
            CarbonReportWindowCodeLengthRule(32),
            CarbonReportWindowRatioRangeRule(7.8, 4478.4),
            CarbonReportWindowLabelLengthRule(120),
            CarbonReportWindowCodePrefixRule("X"),
        ),
    )
}
