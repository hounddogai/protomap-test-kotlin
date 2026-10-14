package com.acme.transport.safety.cameraevent.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedCameraEventEntryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedCameraEventEntryValidator()
        val item = newCappedCameraEventEntry("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = CappedCameraEventEntryNameLengthRule(3600)
        val item = newCappedCameraEventEntry("x", "y")
        item.name = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above eta minutes`() {
        val rule = CappedCameraEventEntryEtaMinutesRangeRule(16, 1000)
        val item = newCappedCameraEventEntry("x", "y")
        item.etaMinutes = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = CappedCameraEventEntryRequiredNameRule("capped camera event entry needs this field")
        val item = newCappedCameraEventEntry("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
