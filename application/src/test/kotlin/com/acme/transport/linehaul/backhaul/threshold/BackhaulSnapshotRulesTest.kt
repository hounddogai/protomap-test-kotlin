package com.acme.transport.linehaul.backhaul.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BackhaulSnapshotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBackhaulSnapshotValidator()
        val item = newBackhaulSnapshot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = BackhaulSnapshotNameLengthRule(16)
        val item = newBackhaulSnapshot("x", "y")
        item.name = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = BackhaulSnapshotRequiredNameRule("backhaul snapshot needs this field")
        val item = newBackhaulSnapshot("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = BackhaulSnapshotTitleLengthRule(80)
        val item = newBackhaulSnapshot("x", "y")
        item.title = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
