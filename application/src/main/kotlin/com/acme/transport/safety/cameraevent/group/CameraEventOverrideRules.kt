package com.acme.transport.safety.cameraevent.group

/** A rule a camera event override must pass. Check returns its problems, or none. */
interface CameraEventOverrideRule {
    fun check(item: CameraEventOverride): MutableList<String>
}

data class CameraEventOverrideOdometerRangeRule(var min: Int, var max: Int) : CameraEventOverrideRule {
    override fun check(item: CameraEventOverride): MutableList<String> {
        val value = item.odometer
        if (value < min || value > max) {
            return mutableListOf<String>("odometer $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CameraEventOverrideTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : CameraEventOverrideRule {
    override fun check(item: CameraEventOverride): MutableList<String> {
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

data class CameraEventOverrideLoadKgRangeRule(var min: Double, var max: Double) : CameraEventOverrideRule {
    override fun check(item: CameraEventOverride): MutableList<String> {
        val value = item.loadKg
        if (value < min || value > max) {
            return mutableListOf<String>("load kg $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CameraEventOverrideCodeLengthRule(var maxLength: Int) : CameraEventOverrideRule {
    override fun check(item: CameraEventOverride): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CameraEventOverrideOwnerIdLengthRule(var maxLength: Int) : CameraEventOverrideRule {
    override fun check(item: CameraEventOverride): MutableList<String> {
        val size = item.ownerId.length
        if (size > maxLength) {
            return mutableListOf<String>("owner id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CameraEventOverrideRequiredOwnerIdRule(var message: String) : CameraEventOverrideRule {
    override fun check(item: CameraEventOverride): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a camera event override. */
data class CameraEventOverrideValidator(var rules: MutableList<CameraEventOverrideRule>) {
    fun validate(item: CameraEventOverride): MutableList<String> {
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

    fun requireValid(item: CameraEventOverride) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CameraEventOverride>): MutableMap<String, MutableList<String>> {
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

fun defaultCameraEventOverrideValidator(): CameraEventOverrideValidator {
    return CameraEventOverrideValidator(
        mutableListOf<CameraEventOverrideRule>(
            CameraEventOverrideOdometerRangeRule(255, 1000),
            CameraEventOverrideTagsLimitRule(86400, mutableListOf<String>("pending", "weighted")),
            CameraEventOverrideLoadKgRangeRule(5.7, 4898.9),
            CameraEventOverrideCodeLengthRule(32),
            CameraEventOverrideOwnerIdLengthRule(80),
            CameraEventOverrideRequiredOwnerIdRule("camera event override needs this field"),
        ),
    )
}
