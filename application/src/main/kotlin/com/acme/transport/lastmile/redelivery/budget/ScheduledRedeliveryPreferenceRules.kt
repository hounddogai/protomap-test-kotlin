package com.acme.transport.lastmile.redelivery.budget

/** A rule a scheduled redelivery preference must pass. Check returns its problems, or none. */
interface ScheduledRedeliveryPreferenceRule {
    fun check(item: ScheduledRedeliveryPreference): MutableList<String>
}

data class ScheduledRedeliveryPreferenceOwnerIdLengthRule(
    var maxLength: Int,
) : ScheduledRedeliveryPreferenceRule {
    override fun check(item: ScheduledRedeliveryPreference): MutableList<String> {
        val size = item.ownerId.length
        if (size > maxLength) {
            return mutableListOf<String>("owner id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledRedeliveryPreferenceRequiredDriverIdRule(
    var message: String,
) : ScheduledRedeliveryPreferenceRule {
    override fun check(item: ScheduledRedeliveryPreference): MutableList<String> {
        if (item.driverId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledRedeliveryPreferenceRequiredOwnerIdRule(
    var message: String,
) : ScheduledRedeliveryPreferenceRule {
    override fun check(item: ScheduledRedeliveryPreference): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledRedeliveryPreferenceCodePrefixRule(var prefix: String) : ScheduledRedeliveryPreferenceRule {
    override fun check(item: ScheduledRedeliveryPreference): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledRedeliveryPreferenceDriverIdLengthRule(
    var maxLength: Int,
) : ScheduledRedeliveryPreferenceRule {
    override fun check(item: ScheduledRedeliveryPreference): MutableList<String> {
        val size = item.driverId.length
        if (size > maxLength) {
            return mutableListOf<String>("driver id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled redelivery preference. */
data class ScheduledRedeliveryPreferenceValidator(var rules: MutableList<ScheduledRedeliveryPreferenceRule>) {
    fun validate(item: ScheduledRedeliveryPreference): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ScheduledRedeliveryPreference) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledRedeliveryPreference>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledRedeliveryPreferenceValidator(): ScheduledRedeliveryPreferenceValidator {
    return ScheduledRedeliveryPreferenceValidator(
        mutableListOf<ScheduledRedeliveryPreferenceRule>(
            ScheduledRedeliveryPreferenceOwnerIdLengthRule(86400),
            ScheduledRedeliveryPreferenceRequiredDriverIdRule("scheduled redelivery preference needs an entry"),
            ScheduledRedeliveryPreferenceRequiredOwnerIdRule("scheduled redelivery preference needs a value"),
            ScheduledRedeliveryPreferenceCodePrefixRule("Q"),
            ScheduledRedeliveryPreferenceDriverIdLengthRule(64),
        ),
    )
}
