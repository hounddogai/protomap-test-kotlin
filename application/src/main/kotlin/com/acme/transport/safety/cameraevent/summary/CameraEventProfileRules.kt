package com.acme.transport.safety.cameraevent.summary

import com.acme.transport.kit.text.normalize

/** A rule a camera event profile must pass. Check returns its problems, or none. */
interface CameraEventProfileRule {
    fun check(item: CameraEventProfile): MutableList<String>
}

data class CameraEventProfileRevisionRangeRule(var min: Int, var max: Int) : CameraEventProfileRule {
    override fun check(item: CameraEventProfile): MutableList<String> {
        val value = item.revision
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("revision $value is below $min")
        }
        if (value > max) {
            problems.add("revision $value is above $max")
        }
        return problems
    }
}

data class CameraEventProfileUpdatedAtRangeRule(var min: Int, var max: Int) : CameraEventProfileRule {
    override fun check(item: CameraEventProfile): MutableList<String> {
        val value = item.updatedAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("updated at $value is below $min")
        }
        if (value > max) {
            problems.add("updated at $value is above $max")
        }
        return problems
    }
}

data class CameraEventProfileCarrierLengthRule(var maxLength: Int) : CameraEventProfileRule {
    override fun check(item: CameraEventProfile): MutableList<String> {
        val size = item.carrier.length
        if (size > maxLength) {
            return mutableListOf<String>("carrier is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CameraEventProfileDistanceKmRangeRule(var min: Double, var max: Double) : CameraEventProfileRule {
    override fun check(item: CameraEventProfile): MutableList<String> {
        val value = item.distanceKm
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("distance km $value is below $min")
        }
        if (value > max) {
            problems.add("distance km $value is above $max")
        }
        return problems
    }
}

data class CameraEventProfileAllowedTitleRule(var allowed: MutableList<String>) : CameraEventProfileRule {
    override fun check(item: CameraEventProfile): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a camera event profile. */
data class CameraEventProfileValidator(var rules: MutableList<CameraEventProfileRule>) {
    fun validate(item: CameraEventProfile): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CameraEventProfile) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CameraEventProfile>): MutableMap<String, MutableList<String>> {
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

fun defaultCameraEventProfileValidator(): CameraEventProfileValidator {
    return CameraEventProfileValidator(
        mutableListOf<CameraEventProfileRule>(
            CameraEventProfileRevisionRangeRule(64, 3600),
            CameraEventProfileUpdatedAtRangeRule(3600, 86400),
            CameraEventProfileCarrierLengthRule(64),
            CameraEventProfileDistanceKmRangeRule(4.1, 261.4),
            CameraEventProfileAllowedTitleRule(mutableListOf<String>("global", "legacy", "manual")),
        ),
    )
}
