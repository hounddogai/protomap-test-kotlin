package com.acme.transport.lastmile.parcel.request

/** A rule a parcel request must pass. Check returns its problems, or none. */
interface ParcelRequestRule {
    fun check(item: ParcelRequest): MutableList<String>
}

data class ParcelRequestVersionRangeRule(var min: Int, var max: Int) : ParcelRequestRule {
    override fun check(item: ParcelRequest): MutableList<String> {
        val value = item.version
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("version $value is below $min")
        }
        if (value > max) {
            problems.add("version $value is above $max")
        }
        return problems
    }
}

data class ParcelRequestChecksumLengthRule(var maxLength: Int) : ParcelRequestRule {
    override fun check(item: ParcelRequest): MutableList<String> {
        val size = item.checksum.length
        if (size > maxLength) {
            return mutableListOf<String>("checksum is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ParcelRequestFloorValueRangeRule(var min: Double, var max: Double) : ParcelRequestRule {
    override fun check(item: ParcelRequest): MutableList<String> {
        val value = item.floorValue
        if (value < min || value > max) {
            return mutableListOf<String>("floor value $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a parcel request. */
data class ParcelRequestValidator(var rules: MutableList<ParcelRequestRule>) {
    fun validate(item: ParcelRequest): MutableList<String> {
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

    fun requireValid(item: ParcelRequest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ParcelRequest>): MutableMap<String, MutableList<String>> {
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

fun defaultParcelRequestValidator(): ParcelRequestValidator {
    return ParcelRequestValidator(
        mutableListOf<ParcelRequestRule>(
            ParcelRequestVersionRangeRule(64, 64),
            ParcelRequestChecksumLengthRule(1000),
            ParcelRequestFloorValueRangeRule(5.4, 1319.0),
        ),
    )
}
