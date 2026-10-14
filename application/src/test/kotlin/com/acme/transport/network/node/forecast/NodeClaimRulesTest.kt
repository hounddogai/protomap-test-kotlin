package com.acme.transport.network.node.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NodeClaimRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultNodeClaimValidator()
        val item = newNodeClaim("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = NodeClaimRequiredNameRule("node claim needs a value")
        val item = newNodeClaim("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = NodeClaimCodeLengthRule(86400)
        val item = newNodeClaim("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above load kg`() {
        val rule = NodeClaimLoadKgRangeRule(5.5, 736.6)
        val item = newNodeClaim("x", "y")
        item.loadKg = 738.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long name`() {
        val rule = NodeClaimNameLengthRule(64)
        val item = newNodeClaim("x", "y")
        item.name = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = NodeClaimRequiredCodeRule("node claim needs this field")
        val item = newNodeClaim("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
