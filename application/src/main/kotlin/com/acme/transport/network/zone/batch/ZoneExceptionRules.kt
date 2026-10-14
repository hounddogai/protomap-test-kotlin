package com.acme.transport.network.zone.batch

/** A rule a zone exception must pass. Check returns its problems, or none. */
interface ZoneExceptionRule {
    fun check(item: ZoneException): MutableList<String>
}

data class ZoneExceptionRequiredPhoneRule(var message: String) : ZoneExceptionRule {
    override fun check(item: ZoneException): MutableList<String> {
        if (item.phone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ZoneExceptionArchivedGuardRule(var limit: Int) : ZoneExceptionRule {
    override fun check(item: ZoneException): MutableList<String> {
        if (item.archived && item.quantity > limit) {
            return mutableListOf<String>("quantity ${item.quantity} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

data class ZoneExceptionMarginRangeRule(var min: Double, var max: Double) : ZoneExceptionRule {
    override fun check(item: ZoneException): MutableList<String> {
        val value = item.margin
        if (value < min || value > max) {
            return mutableListOf<String>("margin $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ZoneExceptionCodePrefixRule(var prefix: String) : ZoneExceptionRule {
    override fun check(item: ZoneException): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a zone exception. */
data class ZoneExceptionValidator(var rules: MutableList<ZoneExceptionRule>) {
    fun validate(item: ZoneException): MutableList<String> {
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

    fun requireValid(item: ZoneException) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ZoneException>): MutableMap<String, MutableList<String>> {
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

fun defaultZoneExceptionValidator(): ZoneExceptionValidator {
    return ZoneExceptionValidator(
        mutableListOf<ZoneExceptionRule>(
            ZoneExceptionRequiredPhoneRule("zone exception needs an entry"),
            ZoneExceptionArchivedGuardRule(120),
            ZoneExceptionMarginRangeRule(3.2, 3555.5),
            ZoneExceptionCodePrefixRule("Q"),
        ),
    )
}
