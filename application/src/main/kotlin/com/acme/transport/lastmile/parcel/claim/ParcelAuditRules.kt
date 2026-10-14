package com.acme.transport.lastmile.parcel.claim

/** A rule a parcel audit must pass. Check returns its problems, or none. */
interface ParcelAuditRule {
    fun check(item: ParcelAudit): MutableList<String>
}

data class ParcelAuditRequiredCodeRule(var message: String) : ParcelAuditRule {
    override fun check(item: ParcelAudit): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ParcelAuditCodePrefixRule(var prefix: String) : ParcelAuditRule {
    override fun check(item: ParcelAudit): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ParcelAuditRequiredDriverIdRule(var message: String) : ParcelAuditRule {
    override fun check(item: ParcelAudit): MutableList<String> {
        if (item.driverId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ParcelAuditRequiredTitleRule(var message: String) : ParcelAuditRule {
    override fun check(item: ParcelAudit): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ParcelAuditDriverIdLengthRule(var maxLength: Int) : ParcelAuditRule {
    override fun check(item: ParcelAudit): MutableList<String> {
        val size = item.driverId.length
        if (size > maxLength) {
            return mutableListOf<String>("driver id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ParcelAuditRequiredChannelRule(var message: String) : ParcelAuditRule {
    override fun check(item: ParcelAudit): MutableList<String> {
        if (item.channel.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a parcel audit. */
data class ParcelAuditValidator(var rules: MutableList<ParcelAuditRule>) {
    fun validate(item: ParcelAudit): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ParcelAudit) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ParcelAudit>): MutableMap<String, MutableList<String>> {
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

fun defaultParcelAuditValidator(): ParcelAuditValidator {
    return ParcelAuditValidator(
        mutableListOf<ParcelAuditRule>(
            ParcelAuditRequiredCodeRule("parcel audit needs a value"),
            ParcelAuditCodePrefixRule("ACM"),
            ParcelAuditRequiredDriverIdRule("parcel audit needs this field"),
            ParcelAuditRequiredTitleRule("parcel audit needs this field"),
            ParcelAuditDriverIdLengthRule(120),
            ParcelAuditRequiredChannelRule("parcel audit needs this field"),
        ),
    )
}
