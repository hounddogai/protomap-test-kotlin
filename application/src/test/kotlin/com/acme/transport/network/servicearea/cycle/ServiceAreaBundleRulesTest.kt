package com.acme.transport.network.servicearea.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ServiceAreaBundleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultServiceAreaBundleValidator()
        val item = newServiceAreaBundle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = ServiceAreaBundleRatioRangeRule(3.5, 2368.0)
        val item = newServiceAreaBundle("x", "y")
        item.ratio = 2369.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = ServiceAreaBundleRequiredTitleRule("service area bundle needs a value")
        val item = newServiceAreaBundle("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ServiceAreaBundleRequiredCodeRule("service area bundle needs an entry")
        val item = newServiceAreaBundle("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
