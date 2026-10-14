package com.acme.transport.linehaul.laneplan.plancheckpoint

/** A rule a regional lane plan cycle must pass. Check returns its problems, or none. */
interface RegionalLanePlanCycleRule {
    fun check(item: RegionalLanePlanCycle): MutableList<String>
}

data class RegionalLanePlanCycleNameLengthRule(var maxLength: Int) : RegionalLanePlanCycleRule {
    override fun check(item: RegionalLanePlanCycle): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalLanePlanCycleRequiredChecksumRule(var message: String) : RegionalLanePlanCycleRule {
    override fun check(item: RegionalLanePlanCycle): MutableList<String> {
        if (item.checksum.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalLanePlanCycleRequiredDriverIdRule(var message: String) : RegionalLanePlanCycleRule {
    override fun check(item: RegionalLanePlanCycle): MutableList<String> {
        if (item.driverId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalLanePlanCycleCodeLengthRule(var maxLength: Int) : RegionalLanePlanCycleRule {
    override fun check(item: RegionalLanePlanCycle): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalLanePlanCycleRequiredCodeRule(var message: String) : RegionalLanePlanCycleRule {
    override fun check(item: RegionalLanePlanCycle): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalLanePlanCycleChecksumLengthRule(var maxLength: Int) : RegionalLanePlanCycleRule {
    override fun check(item: RegionalLanePlanCycle): MutableList<String> {
        val size = item.checksum.length
        if (size > maxLength) {
            return mutableListOf<String>("checksum is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional lane plan cycle. */
data class RegionalLanePlanCycleValidator(var rules: MutableList<RegionalLanePlanCycleRule>) {
    fun validate(item: RegionalLanePlanCycle): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RegionalLanePlanCycle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalLanePlanCycle>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalLanePlanCycleValidator(): RegionalLanePlanCycleValidator {
    return RegionalLanePlanCycleValidator(
        mutableListOf<RegionalLanePlanCycleRule>(
            RegionalLanePlanCycleNameLengthRule(255),
            RegionalLanePlanCycleRequiredChecksumRule("regional lane plan cycle needs this field"),
            RegionalLanePlanCycleRequiredDriverIdRule("regional lane plan cycle needs this field"),
            RegionalLanePlanCycleCodeLengthRule(86400),
            RegionalLanePlanCycleRequiredCodeRule("regional lane plan cycle needs this field"),
            RegionalLanePlanCycleChecksumLengthRule(86400),
        ),
    )
}
