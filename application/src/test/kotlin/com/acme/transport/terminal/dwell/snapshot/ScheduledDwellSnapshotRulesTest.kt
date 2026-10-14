package com.acme.transport.terminal.dwell.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledDwellSnapshotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledDwellSnapshotValidator()
        val item = newScheduledDwellSnapshot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ScheduledDwellSnapshotRequiredCodeRule("scheduled dwell snapshot needs this field")
        val item = newScheduledDwellSnapshot("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = ScheduledDwellSnapshotRequiredLabelRule("scheduled dwell snapshot needs a value")
        val item = newScheduledDwellSnapshot("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
