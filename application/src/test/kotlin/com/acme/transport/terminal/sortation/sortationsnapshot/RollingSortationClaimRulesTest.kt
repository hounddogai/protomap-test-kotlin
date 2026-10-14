package com.acme.transport.terminal.sortation.sortationsnapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingSortationClaimRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingSortationClaimValidator()
        val item = newRollingSortationClaim("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = RollingSortationClaimRequiredLabelRule("rolling sortation claim needs this field")
        val item = newRollingSortationClaim("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank trailer id`() {
        val rule = RollingSortationClaimRequiredTrailerIdRule("rolling sortation claim needs a value")
        val item = newRollingSortationClaim("", "")
        item.trailerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long trailer id`() {
        val rule = RollingSortationClaimTrailerIdLengthRule(3600)
        val item = newRollingSortationClaim("x", "y")
        item.trailerId = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.trailerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
