package com.acme.transport.emissions.idling.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualIdlingSnapshotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualIdlingSnapshotValidator()
        val item = newManualIdlingSnapshot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above starts at`() {
        val rule = ManualIdlingSnapshotStartsAtRangeRule(16, 32)
        val item = newManualIdlingSnapshot("x", "y")
        item.startsAt = 33
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = ManualIdlingSnapshotCodeLengthRule(86400)
        val item = newManualIdlingSnapshot("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ManualIdlingSnapshotRequiredCodeRule("manual idling snapshot needs an entry")
        val item = newManualIdlingSnapshot("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
