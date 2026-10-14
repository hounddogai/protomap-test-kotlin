package com.acme.transport.lastmile.parcel.parcelslot

/** A rule a scheduled parcel digest must pass. Check returns its problems, or none. */
interface ScheduledParcelDigestRule {
    fun check(item: ScheduledParcelDigest): MutableList<String>
}

data class ScheduledParcelDigestRequiredExternalRefRule(var message: String) : ScheduledParcelDigestRule {
    override fun check(item: ScheduledParcelDigest): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledParcelDigestRequiredCodeRule(var message: String) : ScheduledParcelDigestRule {
    override fun check(item: ScheduledParcelDigest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledParcelDigestRequiredLabelRule(var message: String) : ScheduledParcelDigestRule {
    override fun check(item: ScheduledParcelDigest): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled parcel digest. */
data class ScheduledParcelDigestValidator(var rules: MutableList<ScheduledParcelDigestRule>) {
    fun validate(item: ScheduledParcelDigest): MutableList<String> {
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

    fun requireValid(item: ScheduledParcelDigest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledParcelDigest>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledParcelDigestValidator(): ScheduledParcelDigestValidator {
    return ScheduledParcelDigestValidator(
        mutableListOf<ScheduledParcelDigestRule>(
            ScheduledParcelDigestRequiredExternalRefRule("scheduled parcel digest needs this field"),
            ScheduledParcelDigestRequiredCodeRule("scheduled parcel digest needs a value"),
            ScheduledParcelDigestRequiredLabelRule("scheduled parcel digest needs this field"),
        ),
    )
}
