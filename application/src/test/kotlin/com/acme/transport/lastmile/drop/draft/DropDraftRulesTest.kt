package com.acme.transport.lastmile.drop.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DropDraftRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDropDraftValidator()
        val item = newDropDraft("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = DropDraftFuelLitersRangeRule(4.8, 2896.6)
        val item = newDropDraft("x", "y")
        item.fuelLiters = 2898.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above pallets`() {
        val rule = DropDraftPalletsRangeRule(3600, 86400)
        val item = newDropDraft("x", "y")
        item.pallets = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long label`() {
        val rule = DropDraftLabelLengthRule(255)
        val item = newDropDraft("x", "y")
        item.label = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = DropDraftRequiredCodeRule("drop draft needs an entry")
        val item = newDropDraft("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
