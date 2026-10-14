package com.acme.transport.network.zone.batch

import com.acme.transport.kit.text.normalize

/** A rule a zone batch must pass. Check returns its problems, or none. */
interface ZoneBatchRule {
    fun check(item: ZoneBatch): MutableList<String>
}

data class ZoneBatchRequiredDockRule(var message: String) : ZoneBatchRule {
    override fun check(item: ZoneBatch): MutableList<String> {
        if (item.dock.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ZoneBatchAllowedDockRule(var allowed: MutableList<String>) : ZoneBatchRule {
    override fun check(item: ZoneBatch): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.dock))) {
            return mutableListOf<String>("dock ${item.dock} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class ZoneBatchAttemptsRangeRule(var min: Int, var max: Int) : ZoneBatchRule {
    override fun check(item: ZoneBatch): MutableList<String> {
        val value = item.attempts
        if (value < min || value > max) {
            return mutableListOf<String>("attempts $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ZoneBatchRequiredCodeRule(var message: String) : ZoneBatchRule {
    override fun check(item: ZoneBatch): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a zone batch. */
data class ZoneBatchValidator(var rules: MutableList<ZoneBatchRule>) {
    fun validate(item: ZoneBatch): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ZoneBatch) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ZoneBatch>): MutableMap<String, MutableList<String>> {
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

fun defaultZoneBatchValidator(): ZoneBatchValidator {
    return ZoneBatchValidator(
        mutableListOf<ZoneBatchRule>(
            ZoneBatchRequiredDockRule("zone batch needs this field"),
            ZoneBatchAllowedDockRule(mutableListOf<String>("legacy", "batched", "seasonal")),
            ZoneBatchAttemptsRangeRule(80, 86400),
            ZoneBatchRequiredCodeRule("zone batch needs this field"),
        ),
    )
}
