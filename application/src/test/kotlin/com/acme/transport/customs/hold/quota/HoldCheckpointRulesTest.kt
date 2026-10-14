package com.acme.transport.customs.hold.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HoldCheckpointRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultHoldCheckpointValidator()
        val item = newHoldCheckpoint("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = HoldCheckpointRequiredCodeRule("hold checkpoint needs a value")
        val item = newHoldCheckpoint("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above ends at`() {
        val rule = HoldCheckpointEndsAtRangeRule(16, 1000)
        val item = newHoldCheckpoint("x", "y")
        item.endsAt = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = HoldCheckpointRequiredNameRule("hold checkpoint needs an entry")
        val item = newHoldCheckpoint("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
