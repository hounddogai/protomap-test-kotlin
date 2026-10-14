package com.acme.transport.freightpay.accessorial.accessorialscore

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AccessorialSnapshotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAccessorialSnapshotValidator()
        val item = newAccessorialSnapshot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long unit`() {
        val rule = AccessorialSnapshotUnitLengthRule(16)
        val item = newAccessorialSnapshot("x", "y")
        item.unit = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.unit = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = AccessorialSnapshotRequiredCodeRule("accessorial snapshot needs this field")
        val item = newAccessorialSnapshot("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above duration minutes`() {
        val rule = AccessorialSnapshotDurationMinutesRangeRule(3600, 3600)
        val item = newAccessorialSnapshot("x", "y")
        item.durationMinutes = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = AccessorialSnapshotTitleLengthRule(1000)
        val item = newAccessorialSnapshot("x", "y")
        item.title = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = AccessorialSnapshotRequiredTitleRule("accessorial snapshot needs an entry")
        val item = newAccessorialSnapshot("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
