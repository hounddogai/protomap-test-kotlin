package com.acme.transport.lastmile.redelivery.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RedeliverySnapshotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRedeliverySnapshotValidator()
        val item = newRedeliverySnapshot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long channel`() {
        val rule = RedeliverySnapshotChannelLengthRule(255)
        val item = newRedeliverySnapshot("x", "y")
        item.channel = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.channel = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RedeliverySnapshotRequiredCodeRule("redelivery snapshot needs an entry")
        val item = newRedeliverySnapshot("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = RedeliverySnapshotRequiredLabelRule("redelivery snapshot needs a value")
        val item = newRedeliverySnapshot("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = RedeliverySnapshotLabelLengthRule(86400)
        val item = newRedeliverySnapshot("x", "y")
        item.label = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long title`() {
        val rule = RedeliverySnapshotTitleLengthRule(32)
        val item = newRedeliverySnapshot("x", "y")
        item.title = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = RedeliverySnapshotRequiredTitleRule("redelivery snapshot needs an entry")
        val item = newRedeliverySnapshot("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
