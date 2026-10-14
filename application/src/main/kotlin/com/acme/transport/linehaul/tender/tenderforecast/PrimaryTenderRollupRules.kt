package com.acme.transport.linehaul.tender.tenderforecast

import com.acme.transport.kit.text.normalize

/** A rule a primary tender rollup must pass. Check returns its problems, or none. */
interface PrimaryTenderRollupRule {
    fun check(item: PrimaryTenderRollup): MutableList<String>
}

data class PrimaryTenderRollupNameLengthRule(var maxLength: Int) : PrimaryTenderRollupRule {
    override fun check(item: PrimaryTenderRollup): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryTenderRollupAllowedCodeRule(var allowed: MutableList<String>) : PrimaryTenderRollupRule {
    override fun check(item: PrimaryTenderRollup): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is stale")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryTenderRollupFloorValueRangeRule(var min: Double, var max: Double) : PrimaryTenderRollupRule {
    override fun check(item: PrimaryTenderRollup): MutableList<String> {
        val value = item.floorValue
        if (value < min || value > max) {
            return mutableListOf<String>("floor value $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryTenderRollupRequiredCodeRule(var message: String) : PrimaryTenderRollupRule {
    override fun check(item: PrimaryTenderRollup): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary tender rollup. */
data class PrimaryTenderRollupValidator(var rules: MutableList<PrimaryTenderRollupRule>) {
    fun validate(item: PrimaryTenderRollup): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PrimaryTenderRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryTenderRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryTenderRollupValidator(): PrimaryTenderRollupValidator {
    return PrimaryTenderRollupValidator(
        mutableListOf<PrimaryTenderRollupRule>(
            PrimaryTenderRollupNameLengthRule(255),
            PrimaryTenderRollupAllowedCodeRule(mutableListOf<String>()),
            PrimaryTenderRollupFloorValueRangeRule(2.9, 4200.2),
            PrimaryTenderRollupRequiredCodeRule("primary tender rollup needs a value"),
        ),
    )
}
