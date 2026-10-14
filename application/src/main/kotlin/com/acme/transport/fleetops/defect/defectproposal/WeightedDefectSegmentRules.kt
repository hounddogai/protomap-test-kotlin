package com.acme.transport.fleetops.defect.defectproposal

/** A rule a weighted defect segment must pass. Check returns its problems, or none. */
interface WeightedDefectSegmentRule {
    fun check(item: WeightedDefectSegment): MutableList<String>
}

data class WeightedDefectSegmentRequiredLabelRule(var message: String) : WeightedDefectSegmentRule {
    override fun check(item: WeightedDefectSegment): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedDefectSegmentRequiredRegionRule(var message: String) : WeightedDefectSegmentRule {
    override fun check(item: WeightedDefectSegment): MutableList<String> {
        if (item.region.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedDefectSegmentEtaMinutesRangeRule(var min: Int, var max: Int) : WeightedDefectSegmentRule {
    override fun check(item: WeightedDefectSegment): MutableList<String> {
        val value = item.etaMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("eta minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class WeightedDefectSegmentCodePrefixRule(var prefix: String) : WeightedDefectSegmentRule {
    override fun check(item: WeightedDefectSegment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class WeightedDefectSegmentRegionLengthRule(var maxLength: Int) : WeightedDefectSegmentRule {
    override fun check(item: WeightedDefectSegment): MutableList<String> {
        val size = item.region.length
        if (size > maxLength) {
            return mutableListOf<String>("region is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedDefectSegmentWindowDaysRangeRule(var min: Int, var max: Int) : WeightedDefectSegmentRule {
    override fun check(item: WeightedDefectSegment): MutableList<String> {
        val value = item.windowDays
        if (value < min || value > max) {
            return mutableListOf<String>("window days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted defect segment. */
data class WeightedDefectSegmentValidator(var rules: MutableList<WeightedDefectSegmentRule>) {
    fun validate(item: WeightedDefectSegment): MutableList<String> {
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

    fun requireValid(item: WeightedDefectSegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedDefectSegment>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedDefectSegmentValidator(): WeightedDefectSegmentValidator {
    return WeightedDefectSegmentValidator(
        mutableListOf<WeightedDefectSegmentRule>(
            WeightedDefectSegmentRequiredLabelRule("weighted defect segment needs this field"),
            WeightedDefectSegmentRequiredRegionRule("weighted defect segment needs an entry"),
            WeightedDefectSegmentEtaMinutesRangeRule(32, 255),
            WeightedDefectSegmentCodePrefixRule("OPS"),
            WeightedDefectSegmentRegionLengthRule(86400),
            WeightedDefectSegmentWindowDaysRangeRule(16, 1000),
        ),
    )
}
