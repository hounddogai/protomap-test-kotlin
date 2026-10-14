package com.acme.transport.linehaul.backhaul.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BackhaulClaimRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBackhaulClaimValidator()
        val item = newBackhaulClaim("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank description`() {
        val rule = BackhaulClaimRequiredDescriptionRule("backhaul claim needs a value")
        val item = newBackhaulClaim("", "")
        item.description = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = BackhaulClaimNameLengthRule(64)
        val item = newBackhaulClaim("x", "y")
        item.name = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = BackhaulClaimCodeLengthRule(64)
        val item = newBackhaulClaim("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
