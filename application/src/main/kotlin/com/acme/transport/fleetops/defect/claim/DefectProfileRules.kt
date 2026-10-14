package com.acme.transport.fleetops.defect.claim

/** A rule a defect profile must pass. Check returns its problems, or none. */
interface DefectProfileRule {
    fun check(item: DefectProfile): MutableList<String>
}

data class DefectProfileRequiredLabelRule(var message: String) : DefectProfileRule {
    override fun check(item: DefectProfile): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefectProfileCreatedAtRangeRule(var min: Int, var max: Int) : DefectProfileRule {
    override fun check(item: DefectProfile): MutableList<String> {
        val value = item.createdAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("created at $value is below $min")
        }
        if (value > max) {
            problems.add("created at $value is above $max")
        }
        return problems
    }
}

data class DefectProfileChannelLengthRule(var maxLength: Int) : DefectProfileRule {
    override fun check(item: DefectProfile): MutableList<String> {
        val size = item.channel.length
        if (size > maxLength) {
            return mutableListOf<String>("channel is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefectProfileMaximumRangeRule(var min: Double, var max: Double) : DefectProfileRule {
    override fun check(item: DefectProfile): MutableList<String> {
        val value = item.maximum
        if (value < min || value > max) {
            return mutableListOf<String>("maximum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DefectProfileUnitLengthRule(var maxLength: Int) : DefectProfileRule {
    override fun check(item: DefectProfile): MutableList<String> {
        val size = item.unit.length
        if (size > maxLength) {
            return mutableListOf<String>("unit is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a defect profile. */
data class DefectProfileValidator(var rules: MutableList<DefectProfileRule>) {
    fun validate(item: DefectProfile): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefectProfile) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefectProfile>): MutableMap<String, MutableList<String>> {
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

fun defaultDefectProfileValidator(): DefectProfileValidator {
    return DefectProfileValidator(
        mutableListOf<DefectProfileRule>(
            DefectProfileRequiredLabelRule("defect profile needs a value"),
            DefectProfileCreatedAtRangeRule(120, 1000),
            DefectProfileChannelLengthRule(120),
            DefectProfileMaximumRangeRule(5.3, 4651.1),
            DefectProfileUnitLengthRule(80),
        ),
    )
}
