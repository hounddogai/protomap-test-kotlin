package com.acme.transport.terminal.appointment.appointmentscore

import com.acme.transport.kit.text.normalize

/** A rule a batched appointment score must pass. Check returns its problems, or none. */
interface BatchedAppointmentScoreRule {
    fun check(item: BatchedAppointmentScore): MutableList<String>
}

data class BatchedAppointmentScoreRequiredChannelRule(var message: String) : BatchedAppointmentScoreRule {
    override fun check(item: BatchedAppointmentScore): MutableList<String> {
        if (item.channel.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedAppointmentScoreRequiredTimezoneRule(var message: String) : BatchedAppointmentScoreRule {
    override fun check(item: BatchedAppointmentScore): MutableList<String> {
        if (item.timezone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedAppointmentScoreAllowedTitleRule(
    var allowed: MutableList<String>,
) : BatchedAppointmentScoreRule {
    override fun check(item: BatchedAppointmentScore): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is expired")
        }
        return mutableListOf<String>()
    }
}

data class BatchedAppointmentScoreRequiredCodeRule(var message: String) : BatchedAppointmentScoreRule {
    override fun check(item: BatchedAppointmentScore): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedAppointmentScoreTitleLengthRule(var maxLength: Int) : BatchedAppointmentScoreRule {
    override fun check(item: BatchedAppointmentScore): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched appointment score. */
data class BatchedAppointmentScoreValidator(var rules: MutableList<BatchedAppointmentScoreRule>) {
    fun validate(item: BatchedAppointmentScore): MutableList<String> {
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

    fun requireValid(item: BatchedAppointmentScore) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedAppointmentScore>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedAppointmentScoreValidator(): BatchedAppointmentScoreValidator {
    return BatchedAppointmentScoreValidator(
        mutableListOf<BatchedAppointmentScoreRule>(
            BatchedAppointmentScoreRequiredChannelRule("batched appointment score needs a value"),
            BatchedAppointmentScoreRequiredTimezoneRule("batched appointment score needs this field"),
            BatchedAppointmentScoreAllowedTitleRule(mutableListOf<String>("rolling", "batched", "weighted")),
            BatchedAppointmentScoreRequiredCodeRule("batched appointment score needs this field"),
            BatchedAppointmentScoreTitleLengthRule(120),
        ),
    )
}
