package com.acme.transport.emissions.idling.notice

import com.acme.transport.kit.text.normalize

/** A rule a idling notice must pass. Check returns its problems, or none. */
interface IdlingNoticeRule {
    fun check(item: IdlingNotice): MutableList<String>
}

data class IdlingNoticeCodeLengthRule(var maxLength: Int) : IdlingNoticeRule {
    override fun check(item: IdlingNotice): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class IdlingNoticeCarrierLengthRule(var maxLength: Int) : IdlingNoticeRule {
    override fun check(item: IdlingNotice): MutableList<String> {
        val size = item.carrier.length
        if (size > maxLength) {
            return mutableListOf<String>("carrier is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class IdlingNoticeOwnerIdLengthRule(var maxLength: Int) : IdlingNoticeRule {
    override fun check(item: IdlingNotice): MutableList<String> {
        val size = item.ownerId.length
        if (size > maxLength) {
            return mutableListOf<String>("owner id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class IdlingNoticeStartsAtRangeRule(var min: Int, var max: Int) : IdlingNoticeRule {
    override fun check(item: IdlingNotice): MutableList<String> {
        val value = item.startsAt
        if (value < min || value > max) {
            return mutableListOf<String>("starts at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class IdlingNoticeAllowedCodeRule(var allowed: MutableList<String>) : IdlingNoticeRule {
    override fun check(item: IdlingNotice): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is expired")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a idling notice. */
data class IdlingNoticeValidator(var rules: MutableList<IdlingNoticeRule>) {
    fun validate(item: IdlingNotice): MutableList<String> {
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

    fun requireValid(item: IdlingNotice) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<IdlingNotice>): MutableMap<String, MutableList<String>> {
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

fun defaultIdlingNoticeValidator(): IdlingNoticeValidator {
    return IdlingNoticeValidator(
        mutableListOf<IdlingNoticeRule>(
            IdlingNoticeCodeLengthRule(32),
            IdlingNoticeCarrierLengthRule(86400),
            IdlingNoticeOwnerIdLengthRule(86400),
            IdlingNoticeStartsAtRangeRule(64, 3600),
            IdlingNoticeAllowedCodeRule(mutableListOf<String>()),
        ),
    )
}
