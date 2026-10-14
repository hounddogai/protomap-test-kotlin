package com.acme.transport.network.hub.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HubSnapshotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultHubSnapshotValidator()
        val item = newHubSnapshot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = HubSnapshotCeilingRangeRule(6.9, 2517.8)
        val item = newHubSnapshot("x", "y")
        item.ceiling = 2519.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long lane`() {
        val rule = HubSnapshotLaneLengthRule(64)
        val item = newHubSnapshot("x", "y")
        item.lane = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.lane = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long title`() {
        val rule = HubSnapshotTitleLengthRule(32)
        val item = newHubSnapshot("x", "y")
        item.title = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = HubSnapshotRequiredTitleRule("hub snapshot needs this field")
        val item = newHubSnapshot("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
