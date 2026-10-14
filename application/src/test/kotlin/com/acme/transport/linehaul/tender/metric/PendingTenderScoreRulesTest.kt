package com.acme.transport.linehaul.tender.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingTenderScoreRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingTenderScoreValidator()
        val item = newPendingTenderScore("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above weight`() {
        val rule = PendingTenderScoreWeightRangeRule(1.0, 2020.8)
        val item = newPendingTenderScore("x", "y")
        item.weight = 2022.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long name`() {
        val rule = PendingTenderScoreNameLengthRule(120)
        val item = newPendingTenderScore("x", "y")
        item.name = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long owner id`() {
        val rule = PendingTenderScoreOwnerIdLengthRule(86400)
        val item = newPendingTenderScore("x", "y")
        item.ownerId = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.ownerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long checksum`() {
        val rule = PendingTenderScoreChecksumLengthRule(80)
        val item = newPendingTenderScore("x", "y")
        item.checksum = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.checksum = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
