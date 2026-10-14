package com.acme.transport.network.zone.batchedzonemapping

/** A rule a scheduled zone segment must pass. Check returns its problems, or none. */
interface ScheduledZoneSegmentRule {
    fun check(item: ScheduledZoneSegment): MutableList<String>
}

data class ScheduledZoneSegmentPinnedGuardRule(var limit: Int) : ScheduledZoneSegmentRule {
    override fun check(item: ScheduledZoneSegment): MutableList<String> {
        if (item.pinned && item.quantity > limit) {
            return mutableListOf<String>("quantity ${item.quantity} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledZoneSegmentTitleLengthRule(var maxLength: Int) : ScheduledZoneSegmentRule {
    override fun check(item: ScheduledZoneSegment): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledZoneSegmentChannelLengthRule(var maxLength: Int) : ScheduledZoneSegmentRule {
    override fun check(item: ScheduledZoneSegment): MutableList<String> {
        val size = item.channel.length
        if (size > maxLength) {
            return mutableListOf<String>("channel is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledZoneSegmentLaneLengthRule(var maxLength: Int) : ScheduledZoneSegmentRule {
    override fun check(item: ScheduledZoneSegment): MutableList<String> {
        val size = item.lane.length
        if (size > maxLength) {
            return mutableListOf<String>("lane is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledZoneSegmentRequiredChannelRule(var message: String) : ScheduledZoneSegmentRule {
    override fun check(item: ScheduledZoneSegment): MutableList<String> {
        if (item.channel.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled zone segment. */
data class ScheduledZoneSegmentValidator(var rules: MutableList<ScheduledZoneSegmentRule>) {
    fun validate(item: ScheduledZoneSegment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ScheduledZoneSegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledZoneSegment>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledZoneSegmentValidator(): ScheduledZoneSegmentValidator {
    return ScheduledZoneSegmentValidator(
        mutableListOf<ScheduledZoneSegmentRule>(
            ScheduledZoneSegmentPinnedGuardRule(1000),
            ScheduledZoneSegmentTitleLengthRule(64),
            ScheduledZoneSegmentChannelLengthRule(64),
            ScheduledZoneSegmentLaneLengthRule(80),
            ScheduledZoneSegmentRequiredChannelRule("scheduled zone segment needs this field"),
        ),
    )
}
