package com.acme.transport.linehaul.relay.relayquota

/** A rule a rolling relay request must pass. Check returns its problems, or none. */
interface RollingRelayRequestRule {
    fun check(item: RollingRelayRequest): MutableList<String>
}

data class RollingRelayRequestRequiredLabelRule(var message: String) : RollingRelayRequestRule {
    override fun check(item: RollingRelayRequest): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingRelayRequestCodeLengthRule(var maxLength: Int) : RollingRelayRequestRule {
    override fun check(item: RollingRelayRequest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingRelayRequestTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : RollingRelayRequestRule {
    override fun check(item: RollingRelayRequest): MutableList<String> {
        val problems = mutableListOf<String>()
        if (item.tags.size > maxCount) {
            problems.add("has ${item.tags.size} tags, at most $maxCount allowed")
        }
        for (tag in item.tags) {
            if (banned.contains(tag)) {
                problems.add("tag $tag is not allowed")
            }
        }
        return problems
    }
}

data class RollingRelayRequestRequiredCodeRule(var message: String) : RollingRelayRequestRule {
    override fun check(item: RollingRelayRequest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingRelayRequestRequiredReviewerRule(var message: String) : RollingRelayRequestRule {
    override fun check(item: RollingRelayRequest): MutableList<String> {
        if (item.reviewer.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingRelayRequestReviewerLengthRule(var maxLength: Int) : RollingRelayRequestRule {
    override fun check(item: RollingRelayRequest): MutableList<String> {
        val size = item.reviewer.length
        if (size > maxLength) {
            return mutableListOf<String>("reviewer is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling relay request. */
data class RollingRelayRequestValidator(var rules: MutableList<RollingRelayRequestRule>) {
    fun validate(item: RollingRelayRequest): MutableList<String> {
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

    fun requireValid(item: RollingRelayRequest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingRelayRequest>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingRelayRequestValidator(): RollingRelayRequestValidator {
    return RollingRelayRequestValidator(
        mutableListOf<RollingRelayRequestRule>(
            RollingRelayRequestRequiredLabelRule("rolling relay request needs a value"),
            RollingRelayRequestCodeLengthRule(120),
            RollingRelayRequestTagsLimitRule(3600, mutableListOf<String>("global", "lenient")),
            RollingRelayRequestRequiredCodeRule("rolling relay request needs a value"),
            RollingRelayRequestRequiredReviewerRule("rolling relay request needs a value"),
            RollingRelayRequestReviewerLengthRule(255),
        ),
    )
}
