package com.acme.transport.terminal.dwell.policy

/** A rule a rolling dwell mapping must pass. Check returns its problems, or none. */
interface RollingDwellMappingRule {
    fun check(item: RollingDwellMapping): MutableList<String>
}

data class RollingDwellMappingLabelLengthRule(var maxLength: Int) : RollingDwellMappingRule {
    override fun check(item: RollingDwellMapping): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingDwellMappingCodeLengthRule(var maxLength: Int) : RollingDwellMappingRule {
    override fun check(item: RollingDwellMapping): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingDwellMappingRequiredLabelRule(var message: String) : RollingDwellMappingRule {
    override fun check(item: RollingDwellMapping): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingDwellMappingRequiredLaneRule(var message: String) : RollingDwellMappingRule {
    override fun check(item: RollingDwellMapping): MutableList<String> {
        if (item.lane.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingDwellMappingLaneLengthRule(var maxLength: Int) : RollingDwellMappingRule {
    override fun check(item: RollingDwellMapping): MutableList<String> {
        val size = item.lane.length
        if (size > maxLength) {
            return mutableListOf<String>("lane is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling dwell mapping. */
data class RollingDwellMappingValidator(var rules: MutableList<RollingDwellMappingRule>) {
    fun validate(item: RollingDwellMapping): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RollingDwellMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingDwellMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingDwellMappingValidator(): RollingDwellMappingValidator {
    return RollingDwellMappingValidator(
        mutableListOf<RollingDwellMappingRule>(
            RollingDwellMappingLabelLengthRule(120),
            RollingDwellMappingCodeLengthRule(86400),
            RollingDwellMappingRequiredLabelRule("rolling dwell mapping needs an entry"),
            RollingDwellMappingRequiredLaneRule("rolling dwell mapping needs this field"),
            RollingDwellMappingLaneLengthRule(3600),
        ),
    )
}
