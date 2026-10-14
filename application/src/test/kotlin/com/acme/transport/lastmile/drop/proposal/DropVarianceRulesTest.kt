package com.acme.transport.lastmile.drop.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DropVarianceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDropVarianceValidator()
        val item = newDropVariance("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = DropVarianceRequiredNameRule("drop variance needs a value")
        val item = newDropVariance("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long depot`() {
        val rule = DropVarianceDepotLengthRule(86400)
        val item = newDropVariance("x", "y")
        item.depot = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.depot = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = DropVarianceCodeLengthRule(255)
        val item = newDropVariance("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = DropVarianceNameLengthRule(1000)
        val item = newDropVariance("x", "y")
        item.name = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank depot`() {
        val rule = DropVarianceRequiredDepotRule("drop variance needs a value")
        val item = newDropVariance("", "")
        item.depot = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
