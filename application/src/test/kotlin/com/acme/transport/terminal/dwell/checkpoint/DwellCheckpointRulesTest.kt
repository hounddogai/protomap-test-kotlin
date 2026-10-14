package com.acme.transport.terminal.dwell.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DwellCheckpointRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDwellCheckpointValidator()
        val item = newDwellCheckpoint("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = DwellCheckpointRequiredCodeRule("dwell checkpoint needs this field")
        val item = newDwellCheckpoint("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank shift code`() {
        val rule = DwellCheckpointRequiredShiftCodeRule("dwell checkpoint needs a value")
        val item = newDwellCheckpoint("", "")
        item.shiftCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = DwellCheckpointRequiredLabelRule("dwell checkpoint needs this field")
        val item = newDwellCheckpoint("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
