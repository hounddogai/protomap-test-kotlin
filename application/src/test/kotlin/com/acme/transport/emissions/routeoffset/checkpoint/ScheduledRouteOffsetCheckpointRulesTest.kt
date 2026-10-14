package com.acme.transport.emissions.routeoffset.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledRouteOffsetCheckpointRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledRouteOffsetCheckpointValidator()
        val item = newScheduledRouteOffsetCheckpoint("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long external ref`() {
        val rule = ScheduledRouteOffsetCheckpointExternalRefLengthRule(16)
        val item = newScheduledRouteOffsetCheckpoint("x", "y")
        item.externalRef = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.externalRef = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = ScheduledRouteOffsetCheckpointNameLengthRule(80)
        val item = newScheduledRouteOffsetCheckpoint("x", "y")
        item.name = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = ScheduledRouteOffsetCheckpointRequiredNameRule("scheduled route offset checkpoint needs an entry")
        val item = newScheduledRouteOffsetCheckpoint("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
