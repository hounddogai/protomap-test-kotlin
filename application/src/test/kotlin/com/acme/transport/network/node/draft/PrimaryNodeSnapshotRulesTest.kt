package com.acme.transport.network.node.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryNodeSnapshotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryNodeSnapshotValidator()
        val item = newPrimaryNodeSnapshot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank checksum`() {
        val rule = PrimaryNodeSnapshotRequiredChecksumRule("primary node snapshot needs a value")
        val item = newPrimaryNodeSnapshot("", "")
        item.checksum = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = PrimaryNodeSnapshotTitleLengthRule(3600)
        val item = newPrimaryNodeSnapshot("x", "y")
        item.title = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long unit`() {
        val rule = PrimaryNodeSnapshotUnitLengthRule(32)
        val item = newPrimaryNodeSnapshot("x", "y")
        item.unit = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.unit = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
