package com.acme.transport.fleetops.defect.summary

import com.acme.transport.kit.text.normalize

/** A rule a defect request must pass. Check returns its problems, or none. */
interface DefectRequestRule {
    fun check(item: DefectRequest): MutableList<String>
}

data class DefectRequestArchivedGuardRule(var limit: Double) : DefectRequestRule {
    override fun check(item: DefectRequest): MutableList<String> {
        if (item.archived && item.fuelLiters > limit) {
            return mutableListOf<String>("fuel liters ${item.fuelLiters} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

data class DefectRequestChannelLengthRule(var maxLength: Int) : DefectRequestRule {
    override fun check(item: DefectRequest): MutableList<String> {
        val size = item.channel.length
        if (size > maxLength) {
            return mutableListOf<String>("channel is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefectRequestAllowedChannelRule(var allowed: MutableList<String>) : DefectRequestRule {
    override fun check(item: DefectRequest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.channel))) {
            return mutableListOf<String>("channel ${item.channel} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class DefectRequestRequiredNameRule(var message: String) : DefectRequestRule {
    override fun check(item: DefectRequest): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefectRequestSequenceRangeRule(var min: Int, var max: Int) : DefectRequestRule {
    override fun check(item: DefectRequest): MutableList<String> {
        val value = item.sequence
        if (value < min || value > max) {
            return mutableListOf<String>("sequence $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a defect request. */
data class DefectRequestValidator(var rules: MutableList<DefectRequestRule>) {
    fun validate(item: DefectRequest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefectRequest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefectRequest>): MutableMap<String, MutableList<String>> {
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

fun defaultDefectRequestValidator(): DefectRequestValidator {
    return DefectRequestValidator(
        mutableListOf<DefectRequestRule>(
            DefectRequestArchivedGuardRule(5.4),
            DefectRequestChannelLengthRule(80),
            DefectRequestAllowedChannelRule(mutableListOf<String>("adaptive", "batched")),
            DefectRequestRequiredNameRule("defect request needs this field"),
            DefectRequestSequenceRangeRule(80, 1000),
        ),
    )
}
