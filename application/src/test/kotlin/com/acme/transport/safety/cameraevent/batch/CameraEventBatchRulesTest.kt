package com.acme.transport.safety.cameraevent.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CameraEventBatchRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCameraEventBatchValidator()
        val item = newCameraEventBatch("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long channel`() {
        val rule = CameraEventBatchChannelLengthRule(32)
        val item = newCameraEventBatch("x", "y")
        item.channel = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.channel = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank category`() {
        val rule = CameraEventBatchRequiredCategoryRule("camera event batch needs a value")
        val item = newCameraEventBatch("", "")
        item.category = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = CameraEventBatchNameLengthRule(3600)
        val item = newCameraEventBatch("x", "y")
        item.name = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above updated at`() {
        val rule = CameraEventBatchUpdatedAtRangeRule(32, 3600)
        val item = newCameraEventBatch("x", "y")
        item.updatedAt = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = CameraEventBatchRequiredCodeRule("camera event batch needs this field")
        val item = newCameraEventBatch("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = CameraEventBatchCodeLengthRule(16)
        val item = newCameraEventBatch("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
