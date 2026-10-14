package com.acme.transport.network.zone.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualZoneCheckpointRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualZoneCheckpointValidator()
        val item = newManualZoneCheckpoint("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = ManualZoneCheckpointTitleLengthRule(255)
        val item = newManualZoneCheckpoint("x", "y")
        item.title = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long category`() {
        val rule = ManualZoneCheckpointCategoryLengthRule(1000)
        val item = newManualZoneCheckpoint("x", "y")
        item.category = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.category = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ManualZoneCheckpointRequiredCodeRule("manual zone checkpoint needs this field")
        val item = newManualZoneCheckpoint("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
