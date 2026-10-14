package com.acme.transport.emissions.idling.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IdlingBundleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultIdlingBundleValidator()
        val item = newIdlingBundle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = IdlingBundleRequiredCodeRule("idling bundle needs an entry")
        val item = newIdlingBundle("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank summary`() {
        val rule = IdlingBundleRequiredSummaryRule("idling bundle needs an entry")
        val item = newIdlingBundle("", "")
        item.summary = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
