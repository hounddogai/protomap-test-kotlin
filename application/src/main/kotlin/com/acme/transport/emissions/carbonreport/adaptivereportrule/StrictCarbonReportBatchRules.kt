package com.acme.transport.emissions.carbonreport.adaptivereportrule

import com.acme.transport.kit.text.normalize

/** A rule a strict carbon report batch must pass. Check returns its problems, or none. */
interface StrictCarbonReportBatchRule {
    fun check(item: StrictCarbonReportBatch): MutableList<String>
}

data class StrictCarbonReportBatchRequiredCodeRule(var message: String) : StrictCarbonReportBatchRule {
    override fun check(item: StrictCarbonReportBatch): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictCarbonReportBatchAllowedNameRule(
    var allowed: MutableList<String>,
) : StrictCarbonReportBatchRule {
    override fun check(item: StrictCarbonReportBatch): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class StrictCarbonReportBatchRequiredDockRule(var message: String) : StrictCarbonReportBatchRule {
    override fun check(item: StrictCarbonReportBatch): MutableList<String> {
        if (item.dock.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictCarbonReportBatchChannelLengthRule(var maxLength: Int) : StrictCarbonReportBatchRule {
    override fun check(item: StrictCarbonReportBatch): MutableList<String> {
        val size = item.channel.length
        if (size > maxLength) {
            return mutableListOf<String>("channel is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict carbon report batch. */
data class StrictCarbonReportBatchValidator(var rules: MutableList<StrictCarbonReportBatchRule>) {
    fun validate(item: StrictCarbonReportBatch): MutableList<String> {
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

    fun requireValid(item: StrictCarbonReportBatch) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictCarbonReportBatch>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictCarbonReportBatchValidator(): StrictCarbonReportBatchValidator {
    return StrictCarbonReportBatchValidator(
        mutableListOf<StrictCarbonReportBatchRule>(
            StrictCarbonReportBatchRequiredCodeRule("strict carbon report batch needs a value"),
            StrictCarbonReportBatchAllowedNameRule(mutableListOf<String>("lenient")),
            StrictCarbonReportBatchRequiredDockRule("strict carbon report batch needs a value"),
            StrictCarbonReportBatchChannelLengthRule(255),
        ),
    )
}
