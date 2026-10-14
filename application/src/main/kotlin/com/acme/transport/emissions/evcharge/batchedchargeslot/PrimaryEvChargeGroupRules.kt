package com.acme.transport.emissions.evcharge.batchedchargeslot

/** A rule a primary ev charge group must pass. Check returns its problems, or none. */
interface PrimaryEvChargeGroupRule {
    fun check(item: PrimaryEvChargeGroup): MutableList<String>
}

data class PrimaryEvChargeGroupChecksumLengthRule(var maxLength: Int) : PrimaryEvChargeGroupRule {
    override fun check(item: PrimaryEvChargeGroup): MutableList<String> {
        val size = item.checksum.length
        if (size > maxLength) {
            return mutableListOf<String>("checksum is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryEvChargeGroupEtaMinutesRangeRule(var min: Int, var max: Int) : PrimaryEvChargeGroupRule {
    override fun check(item: PrimaryEvChargeGroup): MutableList<String> {
        val value = item.etaMinutes
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("eta minutes $value is below $min")
        }
        if (value > max) {
            problems.add("eta minutes $value is above $max")
        }
        return problems
    }
}

data class PrimaryEvChargeGroupFloorValueRangeRule(
    var min: Double,
    var max: Double,
) : PrimaryEvChargeGroupRule {
    override fun check(item: PrimaryEvChargeGroup): MutableList<String> {
        val value = item.floorValue
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("floor value $value is below $min")
        }
        if (value > max) {
            problems.add("floor value $value is above $max")
        }
        return problems
    }
}

data class PrimaryEvChargeGroupRequiredNameRule(var message: String) : PrimaryEvChargeGroupRule {
    override fun check(item: PrimaryEvChargeGroup): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryEvChargeGroupRequiredCodeRule(var message: String) : PrimaryEvChargeGroupRule {
    override fun check(item: PrimaryEvChargeGroup): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary ev charge group. */
data class PrimaryEvChargeGroupValidator(var rules: MutableList<PrimaryEvChargeGroupRule>) {
    fun validate(item: PrimaryEvChargeGroup): MutableList<String> {
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

    fun requireValid(item: PrimaryEvChargeGroup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryEvChargeGroup>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryEvChargeGroupValidator(): PrimaryEvChargeGroupValidator {
    return PrimaryEvChargeGroupValidator(
        mutableListOf<PrimaryEvChargeGroupRule>(
            PrimaryEvChargeGroupChecksumLengthRule(16),
            PrimaryEvChargeGroupEtaMinutesRangeRule(64, 86400),
            PrimaryEvChargeGroupFloorValueRangeRule(9.1, 908.7),
            PrimaryEvChargeGroupRequiredNameRule("primary ev charge group needs this field"),
            PrimaryEvChargeGroupRequiredCodeRule("primary ev charge group needs a value"),
        ),
    )
}
