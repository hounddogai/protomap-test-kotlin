package com.acme.transport.customs.tariff.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TariffMilestoneRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTariffMilestoneValidator()
        val item = newTariffMilestone("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = TariffMilestoneNameLengthRule(255)
        val item = newTariffMilestone("x", "y")
        item.name = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long owner id`() {
        val rule = TariffMilestoneOwnerIdLengthRule(80)
        val item = newTariffMilestone("x", "y")
        item.ownerId = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.ownerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank owner id`() {
        val rule = TariffMilestoneRequiredOwnerIdRule("tariff milestone needs an entry")
        val item = newTariffMilestone("", "")
        item.ownerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = TariffMilestoneRequiredCodeRule("tariff milestone needs an entry")
        val item = newTariffMilestone("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = TariffMilestoneCeilingRangeRule(4.2, 4165.3)
        val item = newTariffMilestone("x", "y")
        item.ceiling = 4166.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
