package com.acme.transport.safety.cameraevent.batch

/** A rule a camera event batch must pass. Check returns its problems, or none. */
interface CameraEventBatchRule {
    fun check(item: CameraEventBatch): MutableList<String>
}

data class CameraEventBatchChannelLengthRule(var maxLength: Int) : CameraEventBatchRule {
    override fun check(item: CameraEventBatch): MutableList<String> {
        val size = item.channel.length
        if (size > maxLength) {
            return mutableListOf<String>("channel is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CameraEventBatchRequiredCategoryRule(var message: String) : CameraEventBatchRule {
    override fun check(item: CameraEventBatch): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CameraEventBatchNameLengthRule(var maxLength: Int) : CameraEventBatchRule {
    override fun check(item: CameraEventBatch): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CameraEventBatchUpdatedAtRangeRule(var min: Int, var max: Int) : CameraEventBatchRule {
    override fun check(item: CameraEventBatch): MutableList<String> {
        val value = item.updatedAt
        if (value < min || value > max) {
            return mutableListOf<String>("updated at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CameraEventBatchRequiredCodeRule(var message: String) : CameraEventBatchRule {
    override fun check(item: CameraEventBatch): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CameraEventBatchCodeLengthRule(var maxLength: Int) : CameraEventBatchRule {
    override fun check(item: CameraEventBatch): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a camera event batch. */
data class CameraEventBatchValidator(var rules: MutableList<CameraEventBatchRule>) {
    fun validate(item: CameraEventBatch): MutableList<String> {
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

    fun requireValid(item: CameraEventBatch) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CameraEventBatch>): MutableMap<String, MutableList<String>> {
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

fun defaultCameraEventBatchValidator(): CameraEventBatchValidator {
    return CameraEventBatchValidator(
        mutableListOf<CameraEventBatchRule>(
            CameraEventBatchChannelLengthRule(1000),
            CameraEventBatchRequiredCategoryRule("camera event batch needs this field"),
            CameraEventBatchNameLengthRule(255),
            CameraEventBatchUpdatedAtRangeRule(32, 1000),
            CameraEventBatchRequiredCodeRule("camera event batch needs a value"),
            CameraEventBatchCodeLengthRule(255),
        ),
    )
}
