package com.acme.transport.safety.cameraevent.audit

/** A rule a camera event quota must pass. Check returns its problems, or none. */
interface CameraEventQuotaRule {
    fun check(item: CameraEventQuota): MutableList<String>
}

data class CameraEventQuotaRequiredSourceRule(var message: String) : CameraEventQuotaRule {
    override fun check(item: CameraEventQuota): MutableList<String> {
        if (item.source.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CameraEventQuotaNameLengthRule(var maxLength: Int) : CameraEventQuotaRule {
    override fun check(item: CameraEventQuota): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CameraEventQuotaSourceLengthRule(var maxLength: Int) : CameraEventQuotaRule {
    override fun check(item: CameraEventQuota): MutableList<String> {
        val size = item.source.length
        if (size > maxLength) {
            return mutableListOf<String>("source is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CameraEventQuotaCodePrefixRule(var prefix: String) : CameraEventQuotaRule {
    override fun check(item: CameraEventQuota): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class CameraEventQuotaExternalRefLengthRule(var maxLength: Int) : CameraEventQuotaRule {
    override fun check(item: CameraEventQuota): MutableList<String> {
        val size = item.externalRef.length
        if (size > maxLength) {
            return mutableListOf<String>("external ref is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CameraEventQuotaRequiredCodeRule(var message: String) : CameraEventQuotaRule {
    override fun check(item: CameraEventQuota): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a camera event quota. */
data class CameraEventQuotaValidator(var rules: MutableList<CameraEventQuotaRule>) {
    fun validate(item: CameraEventQuota): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CameraEventQuota) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CameraEventQuota>): MutableMap<String, MutableList<String>> {
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

fun defaultCameraEventQuotaValidator(): CameraEventQuotaValidator {
    return CameraEventQuotaValidator(
        mutableListOf<CameraEventQuotaRule>(
            CameraEventQuotaRequiredSourceRule("camera event quota needs a value"),
            CameraEventQuotaNameLengthRule(120),
            CameraEventQuotaSourceLengthRule(16),
            CameraEventQuotaCodePrefixRule("REF"),
            CameraEventQuotaExternalRefLengthRule(80),
            CameraEventQuotaRequiredCodeRule("camera event quota needs an entry"),
        ),
    )
}
