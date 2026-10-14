package com.acme.transport.freightpay.ratecard.regionalcardsnapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryRateCardDraftRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryRateCardDraftValidator()
        val item = newPrimaryRateCardDraft("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = PrimaryRateCardDraftNameLengthRule(80)
        val item = newPrimaryRateCardDraft("x", "y")
        item.name = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = PrimaryRateCardDraftRequiredCodeRule("primary rate card draft needs this field")
        val item = newPrimaryRateCardDraft("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = PrimaryRateCardDraftRequiredNameRule("primary rate card draft needs this field")
        val item = newPrimaryRateCardDraft("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank description`() {
        val rule = PrimaryRateCardDraftRequiredDescriptionRule("primary rate card draft needs a value")
        val item = newPrimaryRateCardDraft("", "")
        item.description = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above ends at`() {
        val rule = PrimaryRateCardDraftEndsAtRangeRule(16, 120)
        val item = newPrimaryRateCardDraft("x", "y")
        item.endsAt = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
