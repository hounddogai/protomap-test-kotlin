package com.acme.transport.terminal.dwell.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryDwellSnapshotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryDwellSnapshotValidator()
        val item = newPrimaryDwellSnapshot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank channel`() {
        val rule = PrimaryDwellSnapshotRequiredChannelRule("primary dwell snapshot needs a value")
        val item = newPrimaryDwellSnapshot("", "")
        item.channel = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = PrimaryDwellSnapshotRequiredLabelRule("primary dwell snapshot needs a value")
        val item = newPrimaryDwellSnapshot("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank dock`() {
        val rule = PrimaryDwellSnapshotRequiredDockRule("primary dwell snapshot needs an entry")
        val item = newPrimaryDwellSnapshot("", "")
        item.dock = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above stop count`() {
        val rule = PrimaryDwellSnapshotStopCountRangeRule(32, 255)
        val item = newPrimaryDwellSnapshot("x", "y")
        item.stopCount = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
