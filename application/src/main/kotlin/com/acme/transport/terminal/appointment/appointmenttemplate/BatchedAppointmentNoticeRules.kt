package com.acme.transport.terminal.appointment.appointmenttemplate

/** A rule a batched appointment notice must pass. Check returns its problems, or none. */
interface BatchedAppointmentNoticeRule {
    fun check(item: BatchedAppointmentNotice): MutableList<String>
}

data class BatchedAppointmentNoticeRequiredCurrencyRule(var message: String) : BatchedAppointmentNoticeRule {
    override fun check(item: BatchedAppointmentNotice): MutableList<String> {
        if (item.currency.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedAppointmentNoticeCodeLengthRule(var maxLength: Int) : BatchedAppointmentNoticeRule {
    override fun check(item: BatchedAppointmentNotice): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedAppointmentNoticeRequiredCodeRule(var message: String) : BatchedAppointmentNoticeRule {
    override fun check(item: BatchedAppointmentNotice): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedAppointmentNoticeCodePrefixRule(var prefix: String) : BatchedAppointmentNoticeRule {
    override fun check(item: BatchedAppointmentNotice): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class BatchedAppointmentNoticeRequiredRouteCodeRule(var message: String) : BatchedAppointmentNoticeRule {
    override fun check(item: BatchedAppointmentNotice): MutableList<String> {
        if (item.routeCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched appointment notice. */
data class BatchedAppointmentNoticeValidator(var rules: MutableList<BatchedAppointmentNoticeRule>) {
    fun validate(item: BatchedAppointmentNotice): MutableList<String> {
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

    fun requireValid(item: BatchedAppointmentNotice) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedAppointmentNotice>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedAppointmentNoticeValidator(): BatchedAppointmentNoticeValidator {
    return BatchedAppointmentNoticeValidator(
        mutableListOf<BatchedAppointmentNoticeRule>(
            BatchedAppointmentNoticeRequiredCurrencyRule("batched appointment notice needs an entry"),
            BatchedAppointmentNoticeCodeLengthRule(32),
            BatchedAppointmentNoticeRequiredCodeRule("batched appointment notice needs this field"),
            BatchedAppointmentNoticeCodePrefixRule("ACM"),
            BatchedAppointmentNoticeRequiredRouteCodeRule("batched appointment notice needs a value"),
        ),
    )
}
