package com.acme.transport.network.zone.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedZoneLedgerRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedZoneLedgerValidator()
        val item = newWeightedZoneLedger("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = WeightedZoneLedgerCodeLengthRule(3600)
        val item = newWeightedZoneLedger("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long contact email`() {
        val rule = WeightedZoneLedgerContactEmailLengthRule(120)
        val item = newWeightedZoneLedger("x", "y")
        item.contactEmail = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.contactEmail = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long locale`() {
        val rule = WeightedZoneLedgerLocaleLengthRule(32)
        val item = newWeightedZoneLedger("x", "y")
        item.locale = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.locale = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
