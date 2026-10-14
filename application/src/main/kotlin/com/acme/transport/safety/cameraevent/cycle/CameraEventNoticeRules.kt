package com.acme.transport.safety.cameraevent.cycle

/** A rule a camera event notice must pass. Check returns its problems, or none. */
interface CameraEventNoticeRule {
    fun check(item: CameraEventNotice): MutableList<String>
}

data class CameraEventNoticeRequiredCodeRule(var message: String) : CameraEventNoticeRule {
    override fun check(item: CameraEventNotice): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CameraEventNoticeRequiredLabelRule(var message: String) : CameraEventNoticeRule {
    override fun check(item: CameraEventNotice): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CameraEventNoticeRequiredContactEmailRule(var message: String) : CameraEventNoticeRule {
    override fun check(item: CameraEventNotice): MutableList<String> {
        if (item.contactEmail.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CameraEventNoticeLabelLengthRule(var maxLength: Int) : CameraEventNoticeRule {
    override fun check(item: CameraEventNotice): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a camera event notice. */
data class CameraEventNoticeValidator(var rules: MutableList<CameraEventNoticeRule>) {
    fun validate(item: CameraEventNotice): MutableList<String> {
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

    fun requireValid(item: CameraEventNotice) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CameraEventNotice>): MutableMap<String, MutableList<String>> {
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

fun defaultCameraEventNoticeValidator(): CameraEventNoticeValidator {
    return CameraEventNoticeValidator(
        mutableListOf<CameraEventNoticeRule>(
            CameraEventNoticeRequiredCodeRule("camera event notice needs an entry"),
            CameraEventNoticeRequiredLabelRule("camera event notice needs an entry"),
            CameraEventNoticeRequiredContactEmailRule("camera event notice needs a value"),
            CameraEventNoticeLabelLengthRule(64),
        ),
    )
}
