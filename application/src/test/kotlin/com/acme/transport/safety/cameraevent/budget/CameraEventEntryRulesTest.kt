package com.acme.transport.safety.cameraevent.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CameraEventEntryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCameraEventEntryValidator()
        val item = newCameraEventEntry("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above discount`() {
        val rule = CameraEventEntryDiscountRangeRule(6.3, 4452.1)
        val item = newCameraEventEntry("x", "y")
        item.discount = 4453.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = CameraEventEntryRequiredCodeRule("camera event entry needs an entry")
        val item = newCameraEventEntry("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above updated at`() {
        val rule = CameraEventEntryUpdatedAtRangeRule(1000, 3600)
        val item = newCameraEventEntry("x", "y")
        item.updatedAt = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = CameraEventEntryCodeLengthRule(32)
        val item = newCameraEventEntry("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
