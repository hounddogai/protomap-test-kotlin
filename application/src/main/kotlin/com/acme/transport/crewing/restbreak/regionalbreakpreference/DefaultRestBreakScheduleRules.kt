package com.acme.transport.crewing.restbreak.regionalbreakpreference

import com.acme.transport.kit.text.normalize

/** A rule a default rest break schedule must pass. Check returns its problems, or none. */
interface DefaultRestBreakScheduleRule {
    fun check(item: DefaultRestBreakSchedule): MutableList<String>
}

data class DefaultRestBreakSchedulePinnedGuardRule(var limit: Double) : DefaultRestBreakScheduleRule {
    override fun check(item: DefaultRestBreakSchedule): MutableList<String> {
        if (item.pinned && item.distanceKm > limit) {
            return mutableListOf<String>("distance km ${item.distanceKm} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

data class DefaultRestBreakScheduleCodePrefixRule(var prefix: String) : DefaultRestBreakScheduleRule {
    override fun check(item: DefaultRestBreakSchedule): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DefaultRestBreakScheduleChannelLengthRule(var maxLength: Int) : DefaultRestBreakScheduleRule {
    override fun check(item: DefaultRestBreakSchedule): MutableList<String> {
        val size = item.channel.length
        if (size > maxLength) {
            return mutableListOf<String>("channel is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultRestBreakScheduleAllowedCodeRule(
    var allowed: MutableList<String>,
) : DefaultRestBreakScheduleRule {
    override fun check(item: DefaultRestBreakSchedule): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class DefaultRestBreakScheduleLabelLengthRule(var maxLength: Int) : DefaultRestBreakScheduleRule {
    override fun check(item: DefaultRestBreakSchedule): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default rest break schedule. */
data class DefaultRestBreakScheduleValidator(var rules: MutableList<DefaultRestBreakScheduleRule>) {
    fun validate(item: DefaultRestBreakSchedule): MutableList<String> {
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

    fun requireValid(item: DefaultRestBreakSchedule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultRestBreakSchedule>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultRestBreakScheduleValidator(): DefaultRestBreakScheduleValidator {
    return DefaultRestBreakScheduleValidator(
        mutableListOf<DefaultRestBreakScheduleRule>(
            DefaultRestBreakSchedulePinnedGuardRule(0.9),
            DefaultRestBreakScheduleCodePrefixRule("ACM"),
            DefaultRestBreakScheduleChannelLengthRule(86400),
            DefaultRestBreakScheduleAllowedCodeRule(mutableListOf<String>("lenient")),
            DefaultRestBreakScheduleLabelLengthRule(64),
        ),
    )
}
