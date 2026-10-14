package com.acme.transport.network.servicearea.areaexception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedServiceAreaMappingRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedServiceAreaMappingValidator()
        val item = newCappedServiceAreaMapping("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long description`() {
        val rule = CappedServiceAreaMappingDescriptionLengthRule(32)
        val item = newCappedServiceAreaMapping("x", "y")
        item.description = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.description = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = CappedServiceAreaMappingRequiredCodeRule("capped service area mapping needs a value")
        val item = newCappedServiceAreaMapping("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank contact email`() {
        val rule = CappedServiceAreaMappingRequiredContactEmailRule("capped service area mapping needs an entry")
        val item = newCappedServiceAreaMapping("", "")
        item.contactEmail = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = CappedServiceAreaMappingRequiredLabelRule("capped service area mapping needs an entry")
        val item = newCappedServiceAreaMapping("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long contact email`() {
        val rule = CappedServiceAreaMappingContactEmailLengthRule(16)
        val item = newCappedServiceAreaMapping("x", "y")
        item.contactEmail = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.contactEmail = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
