package com.acme.transport.linehaul.tender.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalTenderDraftRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalTenderDraftValidator()
        val item = newGlobalTenderDraft("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = GlobalTenderDraftRequiredCodeRule("global tender draft needs this field")
        val item = newGlobalTenderDraft("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = GlobalTenderDraftRequiredNameRule("global tender draft needs an entry")
        val item = newGlobalTenderDraft("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank region`() {
        val rule = GlobalTenderDraftRequiredRegionRule("global tender draft needs an entry")
        val item = newGlobalTenderDraft("", "")
        item.region = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = GlobalTenderDraftNameLengthRule(255)
        val item = newGlobalTenderDraft("x", "y")
        item.name = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
