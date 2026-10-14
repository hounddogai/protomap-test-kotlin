package com.acme.transport.freightpay.dispute.group

/** A rule a dispute notice must pass. Check returns its problems, or none. */
interface DisputeNoticeRule {
    fun check(item: DisputeNotice): MutableList<String>
}

data class DisputeNoticeCodeLengthRule(var maxLength: Int) : DisputeNoticeRule {
    override fun check(item: DisputeNotice): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DisputeNoticeCodePrefixRule(var prefix: String) : DisputeNoticeRule {
    override fun check(item: DisputeNotice): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DisputeNoticeRequiredDescriptionRule(var message: String) : DisputeNoticeRule {
    override fun check(item: DisputeNotice): MutableList<String> {
        if (item.description.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DisputeNoticeRequiredOwnerIdRule(var message: String) : DisputeNoticeRule {
    override fun check(item: DisputeNotice): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DisputeNoticeRequiredCodeRule(var message: String) : DisputeNoticeRule {
    override fun check(item: DisputeNotice): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DisputeNoticeDescriptionLengthRule(var maxLength: Int) : DisputeNoticeRule {
    override fun check(item: DisputeNotice): MutableList<String> {
        val size = item.description.length
        if (size > maxLength) {
            return mutableListOf<String>("description is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a dispute notice. */
data class DisputeNoticeValidator(var rules: MutableList<DisputeNoticeRule>) {
    fun validate(item: DisputeNotice): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DisputeNotice) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DisputeNotice>): MutableMap<String, MutableList<String>> {
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

fun defaultDisputeNoticeValidator(): DisputeNoticeValidator {
    return DisputeNoticeValidator(
        mutableListOf<DisputeNoticeRule>(
            DisputeNoticeCodeLengthRule(3600),
            DisputeNoticeCodePrefixRule("REF"),
            DisputeNoticeRequiredDescriptionRule("dispute notice needs this field"),
            DisputeNoticeRequiredOwnerIdRule("dispute notice needs this field"),
            DisputeNoticeRequiredCodeRule("dispute notice needs this field"),
            DisputeNoticeDescriptionLengthRule(1000),
        ),
    )
}
