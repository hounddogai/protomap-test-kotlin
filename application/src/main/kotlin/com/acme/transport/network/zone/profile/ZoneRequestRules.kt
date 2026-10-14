package com.acme.transport.network.zone.profile

/** A rule a zone request must pass. Check returns its problems, or none. */
interface ZoneRequestRule {
    fun check(item: ZoneRequest): MutableList<String>
}

data class ZoneRequestRequiredExternalRefRule(var message: String) : ZoneRequestRule {
    override fun check(item: ZoneRequest): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ZoneRequestActiveGuardRule(var limit: Double) : ZoneRequestRule {
    override fun check(item: ZoneRequest): MutableList<String> {
        if (item.active && item.floorValue > limit) {
            return mutableListOf<String>("floor value ${item.floorValue} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

data class ZoneRequestCodePrefixRule(var prefix: String) : ZoneRequestRule {
    override fun check(item: ZoneRequest): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ZoneRequestFloorValueRangeRule(var min: Double, var max: Double) : ZoneRequestRule {
    override fun check(item: ZoneRequest): MutableList<String> {
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

data class ZoneRequestCodeLengthRule(var maxLength: Int) : ZoneRequestRule {
    override fun check(item: ZoneRequest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ZoneRequestNameLengthRule(var maxLength: Int) : ZoneRequestRule {
    override fun check(item: ZoneRequest): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a zone request. */
data class ZoneRequestValidator(var rules: MutableList<ZoneRequestRule>) {
    fun validate(item: ZoneRequest): MutableList<String> {
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

    fun requireValid(item: ZoneRequest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ZoneRequest>): MutableMap<String, MutableList<String>> {
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

fun defaultZoneRequestValidator(): ZoneRequestValidator {
    return ZoneRequestValidator(
        mutableListOf<ZoneRequestRule>(
            ZoneRequestRequiredExternalRefRule("zone request needs an entry"),
            ZoneRequestActiveGuardRule(5.9),
            ZoneRequestCodePrefixRule("Q"),
            ZoneRequestFloorValueRangeRule(6.1, 3598.8),
            ZoneRequestCodeLengthRule(120),
            ZoneRequestNameLengthRule(32),
        ),
    )
}
