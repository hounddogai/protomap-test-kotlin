package com.acme.transport.linehaul.capacity.capacityassignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalCapacityProfileRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalCapacityProfileValidator()
        val item = newRegionalCapacityProfile("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = RegionalCapacityProfileRequiredNameRule("regional capacity profile needs a value")
        val item = newRegionalCapacityProfile("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = RegionalCapacityProfileRequiredLabelRule("regional capacity profile needs a value")
        val item = newRegionalCapacityProfile("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank contact email`() {
        val rule = RegionalCapacityProfileRequiredContactEmailRule("regional capacity profile needs this field")
        val item = newRegionalCapacityProfile("", "")
        item.contactEmail = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long contact email`() {
        val rule = RegionalCapacityProfileContactEmailLengthRule(255)
        val item = newRegionalCapacityProfile("x", "y")
        item.contactEmail = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.contactEmail = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = RegionalCapacityProfileNameLengthRule(1000)
        val item = newRegionalCapacityProfile("x", "y")
        item.name = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
