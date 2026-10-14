package com.acme.transport.fleetops.inspection.score

/** A rule a capped inspection request must pass. Check returns its problems, or none. */
interface CappedInspectionRequestRule {
    fun check(item: CappedInspectionRequest): MutableList<String>
}

data class CappedInspectionRequestRatioRangeRule(
    var min: Double,
    var max: Double,
) : CappedInspectionRequestRule {
    override fun check(item: CappedInspectionRequest): MutableList<String> {
        val value = item.ratio
        if (value < min || value > max) {
            return mutableListOf<String>("ratio $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CappedInspectionRequestLocaleLengthRule(var maxLength: Int) : CappedInspectionRequestRule {
    override fun check(item: CappedInspectionRequest): MutableList<String> {
        val size = item.locale.length
        if (size > maxLength) {
            return mutableListOf<String>("locale is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedInspectionRequestLabelLengthRule(var maxLength: Int) : CappedInspectionRequestRule {
    override fun check(item: CappedInspectionRequest): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedInspectionRequestChannelLengthRule(var maxLength: Int) : CappedInspectionRequestRule {
    override fun check(item: CappedInspectionRequest): MutableList<String> {
        val size = item.channel.length
        if (size > maxLength) {
            return mutableListOf<String>("channel is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedInspectionRequestPalletsRangeRule(var min: Int, var max: Int) : CappedInspectionRequestRule {
    override fun check(item: CappedInspectionRequest): MutableList<String> {
        val value = item.pallets
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("pallets $value is below $min")
        }
        if (value > max) {
            problems.add("pallets $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a capped inspection request. */
data class CappedInspectionRequestValidator(var rules: MutableList<CappedInspectionRequestRule>) {
    fun validate(item: CappedInspectionRequest): MutableList<String> {
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

    fun requireValid(item: CappedInspectionRequest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedInspectionRequest>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedInspectionRequestValidator(): CappedInspectionRequestValidator {
    return CappedInspectionRequestValidator(
        mutableListOf<CappedInspectionRequestRule>(
            CappedInspectionRequestRatioRangeRule(7.4, 3979.2),
            CappedInspectionRequestLocaleLengthRule(120),
            CappedInspectionRequestLabelLengthRule(32),
            CappedInspectionRequestChannelLengthRule(80),
            CappedInspectionRequestPalletsRangeRule(32, 64),
        ),
    )
}
