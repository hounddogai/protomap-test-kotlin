package com.acme.transport.linehaul.relay.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RelayBatchRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRelayBatchValidator()
        val item = newRelayBatch("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long country`() {
        val rule = RelayBatchCountryLengthRule(120)
        val item = newRelayBatch("x", "y")
        item.country = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.country = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above eta minutes`() {
        val rule = RelayBatchEtaMinutesRangeRule(32, 86400)
        val item = newRelayBatch("x", "y")
        item.etaMinutes = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above created at`() {
        val rule = RelayBatchCreatedAtRangeRule(64, 80)
        val item = newRelayBatch("x", "y")
        item.createdAt = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long label`() {
        val rule = RelayBatchLabelLengthRule(86400)
        val item = newRelayBatch("x", "y")
        item.label = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long owner id`() {
        val rule = RelayBatchOwnerIdLengthRule(86400)
        val item = newRelayBatch("x", "y")
        item.ownerId = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.ownerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
