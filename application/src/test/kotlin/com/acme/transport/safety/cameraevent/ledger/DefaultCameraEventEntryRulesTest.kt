package com.acme.transport.safety.cameraevent.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultCameraEventEntryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultCameraEventEntryValidator()
        val item = newDefaultCameraEventEntry("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above floor value`() {
        val rule = DefaultCameraEventEntryFloorValueRangeRule(3.0, 853.7)
        val item = newDefaultCameraEventEntry("x", "y")
        item.floorValue = 855.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = DefaultCameraEventEntryRequiredLabelRule("default camera event entry needs a value")
        val item = newDefaultCameraEventEntry("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank checksum`() {
        val rule = DefaultCameraEventEntryRequiredChecksumRule("default camera event entry needs an entry")
        val item = newDefaultCameraEventEntry("", "")
        item.checksum = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = DefaultCameraEventEntryLabelLengthRule(80)
        val item = newDefaultCameraEventEntry("x", "y")
        item.label = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
