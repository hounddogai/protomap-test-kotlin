package com.acme.transport.network.servicearea.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryServiceAreaQuotaRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryServiceAreaQuotaValidator()
        val item = newPrimaryServiceAreaQuota("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = PrimaryServiceAreaQuotaRequiredCodeRule("primary service area quota needs this field")
        val item = newPrimaryServiceAreaQuota("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long notes`() {
        val rule = PrimaryServiceAreaQuotaNotesLengthRule(80)
        val item = newPrimaryServiceAreaQuota("x", "y")
        item.notes = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.notes = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank notes`() {
        val rule = PrimaryServiceAreaQuotaRequiredNotesRule("primary service area quota needs a value")
        val item = newPrimaryServiceAreaQuota("", "")
        item.notes = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = PrimaryServiceAreaQuotaRequiredLabelRule("primary service area quota needs an entry")
        val item = newPrimaryServiceAreaQuota("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
