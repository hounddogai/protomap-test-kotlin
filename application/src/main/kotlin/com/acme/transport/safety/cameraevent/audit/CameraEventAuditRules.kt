package com.acme.transport.safety.cameraevent.audit

/** A rule a camera event audit must pass. Check returns its problems, or none. */
interface CameraEventAuditRule {
    fun check(item: CameraEventAudit): MutableList<String>
}

data class CameraEventAuditSourceLengthRule(var maxLength: Int) : CameraEventAuditRule {
    override fun check(item: CameraEventAudit): MutableList<String> {
        val size = item.source.length
        if (size > maxLength) {
            return mutableListOf<String>("source is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CameraEventAuditDiscountRangeRule(var min: Double, var max: Double) : CameraEventAuditRule {
    override fun check(item: CameraEventAudit): MutableList<String> {
        val value = item.discount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("discount $value is below $min")
        }
        if (value > max) {
            problems.add("discount $value is above $max")
        }
        return problems
    }
}

data class CameraEventAuditRequiredSourceRule(var message: String) : CameraEventAuditRule {
    override fun check(item: CameraEventAudit): MutableList<String> {
        if (item.source.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a camera event audit. */
data class CameraEventAuditValidator(var rules: MutableList<CameraEventAuditRule>) {
    fun validate(item: CameraEventAudit): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CameraEventAudit) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CameraEventAudit>): MutableMap<String, MutableList<String>> {
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

fun defaultCameraEventAuditValidator(): CameraEventAuditValidator {
    return CameraEventAuditValidator(
        mutableListOf<CameraEventAuditRule>(
            CameraEventAuditSourceLengthRule(80),
            CameraEventAuditDiscountRangeRule(3.0, 3035.2),
            CameraEventAuditRequiredSourceRule("camera event audit needs a value"),
        ),
    )
}
