package com.acme.transport.linehaul.relay.segment

import com.acme.transport.kit.text.normalize

/** A rule a pending relay segment must pass. Check returns its problems, or none. */
interface PendingRelaySegmentRule {
    fun check(item: PendingRelaySegment): MutableList<String>
}

data class PendingRelaySegmentRequiredDescriptionRule(var message: String) : PendingRelaySegmentRule {
    override fun check(item: PendingRelaySegment): MutableList<String> {
        if (item.description.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingRelaySegmentRequiredNameRule(var message: String) : PendingRelaySegmentRule {
    override fun check(item: PendingRelaySegment): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingRelaySegmentRequiredCodeRule(var message: String) : PendingRelaySegmentRule {
    override fun check(item: PendingRelaySegment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingRelaySegmentAllowedCodeRule(var allowed: MutableList<String>) : PendingRelaySegmentRule {
    override fun check(item: PendingRelaySegment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is too long")
        }
        return mutableListOf<String>()
    }
}

data class PendingRelaySegmentApprovedGuardRule(var limit: Int) : PendingRelaySegmentRule {
    override fun check(item: PendingRelaySegment): MutableList<String> {
        if (item.approved && item.createdAt > limit) {
            return mutableListOf<String>("created at ${item.createdAt} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending relay segment. */
data class PendingRelaySegmentValidator(var rules: MutableList<PendingRelaySegmentRule>) {
    fun validate(item: PendingRelaySegment): MutableList<String> {
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

    fun requireValid(item: PendingRelaySegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingRelaySegment>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingRelaySegmentValidator(): PendingRelaySegmentValidator {
    return PendingRelaySegmentValidator(
        mutableListOf<PendingRelaySegmentRule>(
            PendingRelaySegmentRequiredDescriptionRule("pending relay segment needs an entry"),
            PendingRelaySegmentRequiredNameRule("pending relay segment needs a value"),
            PendingRelaySegmentRequiredCodeRule("pending relay segment needs this field"),
            PendingRelaySegmentAllowedCodeRule(mutableListOf<String>("lenient", "regional")),
            PendingRelaySegmentApprovedGuardRule(255),
        ),
    )
}
