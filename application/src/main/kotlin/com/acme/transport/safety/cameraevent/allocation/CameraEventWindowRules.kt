package com.acme.transport.safety.cameraevent.allocation

/** A rule a camera event window must pass. Check returns its problems, or none. */
interface CameraEventWindowRule {
    fun check(item: CameraEventWindow): MutableList<String>
}

data class CameraEventWindowTrailerIdLengthRule(var maxLength: Int) : CameraEventWindowRule {
    override fun check(item: CameraEventWindow): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CameraEventWindowRequiredLabelRule(var message: String) : CameraEventWindowRule {
    override fun check(item: CameraEventWindow): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CameraEventWindowLabelLengthRule(var maxLength: Int) : CameraEventWindowRule {
    override fun check(item: CameraEventWindow): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CameraEventWindowCodePrefixRule(var prefix: String) : CameraEventWindowRule {
    override fun check(item: CameraEventWindow): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class CameraEventWindowCodeLengthRule(var maxLength: Int) : CameraEventWindowRule {
    override fun check(item: CameraEventWindow): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CameraEventWindowRequiredSegmentRule(var message: String) : CameraEventWindowRule {
    override fun check(item: CameraEventWindow): MutableList<String> {
        if (item.segment.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a camera event window. */
data class CameraEventWindowValidator(var rules: MutableList<CameraEventWindowRule>) {
    fun validate(item: CameraEventWindow): MutableList<String> {
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

    fun requireValid(item: CameraEventWindow) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CameraEventWindow>): MutableMap<String, MutableList<String>> {
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

fun defaultCameraEventWindowValidator(): CameraEventWindowValidator {
    return CameraEventWindowValidator(
        mutableListOf<CameraEventWindowRule>(
            CameraEventWindowTrailerIdLengthRule(86400),
            CameraEventWindowRequiredLabelRule("camera event window needs a value"),
            CameraEventWindowLabelLengthRule(32),
            CameraEventWindowCodePrefixRule("X"),
            CameraEventWindowCodeLengthRule(255),
            CameraEventWindowRequiredSegmentRule("camera event window needs a value"),
        ),
    )
}
