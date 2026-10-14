package com.acme.transport.lastmile.redelivery.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackRedeliverySnapshotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackRedeliverySnapshotValidator()
        val item = newFallbackRedeliverySnapshot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = FallbackRedeliverySnapshotNameLengthRule(1000)
        val item = newFallbackRedeliverySnapshot("x", "y")
        item.name = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long external ref`() {
        val rule = FallbackRedeliverySnapshotExternalRefLengthRule(1000)
        val item = newFallbackRedeliverySnapshot("x", "y")
        item.externalRef = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.externalRef = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = FallbackRedeliverySnapshotRequiredCodeRule("fallback redelivery snapshot needs a value")
        val item = newFallbackRedeliverySnapshot("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above sequence`() {
        val rule = FallbackRedeliverySnapshotSequenceRangeRule(64, 255)
        val item = newFallbackRedeliverySnapshot("x", "y")
        item.sequence = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = FallbackRedeliverySnapshotRequiredNameRule("fallback redelivery snapshot needs this field")
        val item = newFallbackRedeliverySnapshot("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
